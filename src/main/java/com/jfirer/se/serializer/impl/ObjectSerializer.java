package com.jfirer.se.serializer.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se.ClassInfo;
import com.jfirer.se.InternalByteArray;
import com.jfirer.se.JfireSE;
import com.jfirer.se.serializer.Serializer;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ObjectSerializer implements Serializer
{
    private FieldInfo[]      primitiveFieldInfos;
    private FieldInfo[]      boxFieldInfos;
    private FinalFieldInfo[] finalFieldInfos;
    private JfireSE          jfireSE;
    private Class            clazz;

    public ObjectSerializer(Class clazz, JfireSE jfireSE)
    {
        this.clazz   = clazz;
        this.jfireSE = jfireSE;
        Class       type   = clazz;
        List<Field> fields = new ArrayList<>();
        while (type != Object.class)
        {
            fields.addAll(Arrays.stream(type.getDeclaredFields()).filter(Predicate.not(field -> Modifier.isStatic(field.getModifiers()))).toList());
            type = type.getComponentType();
        }
        primitiveFieldInfos = fields.stream().filter(field -> field.getType().isPrimitive()).sorted(Comparator.comparing(o -> o.getType().getName())).map(FieldInfo::new).toArray(FieldInfo[]::new);
        boxFieldInfos       = fields.stream().filter(field -> ReflectUtil.isPrimitiveBox(field.getType()) || field.getType() == String.class).sorted(Comparator.comparing(o -> o.getType().getName())).map(FieldInfo::new).toArray(FieldInfo[]::new);
        finalFieldInfos     = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                    .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                    .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                    .filter(field -> Modifier.isFinal(field.getType().getModifiers())).sorted(Comparator.comparing(o -> o.getType().getName())).map(FinalFieldInfo::new).toArray(FinalFieldInfo[]::new);
    }

    @Override
    public void writeBytes(InternalByteArray byteArray, Object instance)
    {
    }

    @Data
    class FieldInfo
    {
        int           classId;
        ValueAccessor accessor;

        FieldInfo(Field field)
        {
            classId  = ReflectUtil.getClassId(field.getType());
            accessor = new ValueAccessor(field);
        }

        void write(InternalByteArray byteArray, Object instance)
        {
            switch (classId)
            {
                case ReflectUtil.PRIMITIVE_INT -> byteArray.writeVarInt(accessor.getInt(instance));
                case ReflectUtil.PRIMITIVE_LONG -> byteArray.writeVarLong(accessor.getLong(instance));
                case ReflectUtil.PRIMITIVE_FLOAT -> byteArray.writeFloat(accessor.getFloat(instance));
                case ReflectUtil.PRIMITIVE_DOUBLE -> byteArray.writeDouble(accessor.getDouble(instance));
                case ReflectUtil.PRIMITIVE_BOOL -> byteArray.writePositive(accessor.getBoolean(instance) ? 1 : 0);
                case ReflectUtil.PRIMITIVE_CHAR -> byteArray.writeVarChar(accessor.getChar(instance));
                case ReflectUtil.PRIMITIVE_SHORT -> byteArray.writeShort(accessor.getShort(instance));
                case ReflectUtil.PRIMITIVE_BYTE -> byteArray.put(accessor.getByte(instance));
                case ReflectUtil.CLASS_INT ->
                {
                    Integer value = accessor.getIntObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeVarInt(value);
                    }
                }
                case ReflectUtil.CLASS_LONG ->
                {
                    Long value = accessor.getLongObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeVarLong(value);
                    }
                }
                case ReflectUtil.CLASS_FLOAT ->
                {
                    Float value = accessor.getFloatObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeFloat(value);
                    }
                }
                case ReflectUtil.CLASS_DOUBLE ->
                {
                    Double value = accessor.getDoubleObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeDouble(value);
                    }
                }
                case ReflectUtil.CLASS_BOOL ->
                {
                    Boolean value = accessor.getBooleanObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writePositive(value ? 1 : 0);
                    }
                }
                case ReflectUtil.CLASS_CHAR ->
                {
                    Character value = accessor.getCharObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeVarChar(value);
                    }
                }
                case ReflectUtil.CLASS_SHORT ->
                {
                    Short value = accessor.getShortObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeShort(value);
                    }
                }
                case ReflectUtil.CLASS_BYTE ->
                {
                    Byte value = accessor.getByteObject(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.put(value);
                    }
                }
                case ReflectUtil.CLASS_STRING ->
                {
                    String value = (String) accessor.get(instance);
                    if (value == null)
                    {
                        byteArray.put(JfireSE.NULL);
                    }
                    else
                    {
                        byteArray.put((byte) 01);
                        byteArray.writeString(value);
                    }
                }
            }
        }
    }

    class FinalFieldInfo
    {
        ValueAccessor accessor;
        ClassInfo     classInfo;

        FinalFieldInfo(Field field)
        {
            accessor  = new ValueAccessor(field);
            classInfo = jfireSE.getClassInfo(field.getType());
        }

        void write(InternalByteArray byteArray, Object instance)
        {
            Object value = accessor.get(instance);
            if (value == null)
            {
                byteArray.put(JfireSE.NULL);
            }
            else
            {
                byteArray.put((byte) 07);

            }
        }
    }

    class VariableFieldInfo
    {
        ValueAccessor accessor;
        ClassInfo     classInfo;

        VariableFieldInfo(Field field)
        {
            accessor = new ValueAccessor(field);
        }
    }
}
