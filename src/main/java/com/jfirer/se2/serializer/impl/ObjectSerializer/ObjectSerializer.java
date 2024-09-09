package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.serializer.Serializer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class ObjectSerializer implements Serializer
{
    private Class<?>    clazz;
    private FieldInfo[] fieldInfos;

    public ObjectSerializer(Class<?> clazz, JfireSEImpl jfireSE)
    {
        Class       type   = clazz;
        List<Field> fields = new ArrayList<>();
        while (type != Object.class)
        {
            fields.addAll(Arrays.stream(type.getDeclaredFields()).filter(Predicate.not(field -> Modifier.isStatic(field.getModifiers()))).toList());
            type = type.getSuperclass();
        }
        List<PrimitiveFieldInfo> primitiveFieldInfos = fields.stream().filter(field -> field.getType().isPrimitive())//
                                                             .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                             .map(PrimitiveFieldInfo::new)//
                                                             .toList();
        List<BoxedFieldInfo> boxedFieldInfos = fields.stream().filter(field -> ReflectUtil.isPrimitiveBox(field.getType()) || field.getType() == String.class)//
                                                     .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                     .map(BoxedFieldInfo::new)//
                                                     .toList();
        List<FinalFieldInfo> finalFieldInfos = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                                     .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                                     .filter(field -> field.getType() != String.class)//
                                                     .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                                     .filter(field -> Modifier.isFinal(field.getType().getModifiers()))//
                                                     .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                     .map(field -> new FinalFieldInfo(field, jfireSE))//
                                                     .toList();
        List<VariableFieldInfo> variableFieldInfos = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                                           .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                                           .filter(field -> field.getType() != String.class)//
                                                           .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                                           .filter(field -> !Modifier.isFinal(field.getType().getModifiers()))//
                                                           .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                                           .map(field -> new VariableFieldInfo(field, jfireSE))//
                                                           .toList();
        List<FieldInfo> list = new LinkedList<>();
        list.addAll(primitiveFieldInfos);
        list.addAll(boxedFieldInfos);
        list.addAll(finalFieldInfos);
        list.addAll(variableFieldInfos);
        fieldInfos = list.toArray(FieldInfo[]::new);
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        for (FieldInfo each : fieldInfos)
        {
            each.write(byteArray, instance);
        }
    }
}
