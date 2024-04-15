package com.jfirer.se.serializer.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se.ClassInfo;
import com.jfirer.se.InternalByteArray;
import com.jfirer.se.JfireSE;
import com.jfirer.se.serializer.Serializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class ObjectSerializer implements Serializer
{
    private              FieldInfo[]         primitiveFieldInfos;
    private              FieldInfo[]         boxFieldInfos;
    private              FinalFieldInfo[]    finalFieldInfos;
    private              VariableFieldInfo[] variableFieldInfos;
    private              JfireSE             jfireSE;
    private              Class               clazz;
    private static final Unsafe              UNSAFE = Unsafe.getUnsafe();

    public ObjectSerializer(Class clazz, JfireSE jfireSE)
    {
        this.clazz   = clazz;
        this.jfireSE = jfireSE;
        Class       type   = clazz;
        List<Field> fields = new ArrayList<>();
        while (type != Object.class)
        {
            fields.addAll(Arrays.stream(type.getDeclaredFields()).filter(Predicate.not(field -> Modifier.isStatic(field.getModifiers()))).toList());
            type = type.getSuperclass();
        }
        primitiveFieldInfos = fields.stream().filter(field -> field.getType().isPrimitive()).sorted(Comparator.comparing(o -> o.getType().getName())).map(FieldInfo::new).toArray(FieldInfo[]::new);
        boxFieldInfos       = fields.stream().filter(field -> ReflectUtil.isPrimitiveBox(field.getType()) || field.getType() == String.class).sorted(Comparator.comparing(o -> o.getType().getName())).map(FieldInfo::new).toArray(FieldInfo[]::new);
        finalFieldInfos     = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                    .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                    .filter(field -> field.getType() != String.class)//
                                    .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                    .filter(field -> Modifier.isFinal(field.getType().getModifiers()))//
                                    .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                    .map(FinalFieldInfo::new).toArray(FinalFieldInfo[]::new);
        variableFieldInfos  = fields.stream().filter(Predicate.not(field -> ReflectUtil.isPrimitiveBox(field.getType())))//
                                    .filter(Predicate.not(field -> ReflectUtil.isPrimitive(field.getType())))//
                                    .filter(field -> field.getType() != String.class)//
                                    .filter(field -> field.getType() != void.class && field.getType() != Void.class)//
                                    .filter(field -> !Modifier.isFinal(field.getType().getModifiers()))//
                                    .sorted(Comparator.comparing(o -> o.getType().getName()))//
                                    .map(VariableFieldInfo::new).toArray(VariableFieldInfo[]::new);
    }

    @Override
    public void writeBytes(InternalByteArray byteArray, Object instance)
    {
        for (FieldInfo primitiveFieldInfo : primitiveFieldInfos)
        {
            primitiveFieldInfo.write(byteArray, instance);
        }
        for (FieldInfo boxFieldInfo : boxFieldInfos)
        {
            boxFieldInfo.write(byteArray, instance);
        }
        for (FinalFieldInfo finalFieldInfo : finalFieldInfos)
        {
            finalFieldInfo.write(byteArray, instance);
        }
        for (VariableFieldInfo variableFieldInfo : variableFieldInfos)
        {
            variableFieldInfo.write(byteArray, instance);
        }
    }

    @SneakyThrows
    @Override
    public Object readBytes(InternalByteArray byteArray)
    {
        Object instance = UNSAFE.allocateInstance(clazz);
        for (FieldInfo primitiveFieldInfo : primitiveFieldInfos)
        {
            primitiveFieldInfo.read(byteArray, instance);
        }
        for (FieldInfo boxFieldInfo : boxFieldInfos)
        {
            boxFieldInfo.read(byteArray, instance);
        }
        for (FinalFieldInfo finalFieldInfo : finalFieldInfos)
        {
            finalFieldInfo.read(byteArray, instance);
        }
        for (VariableFieldInfo variableFieldInfo : variableFieldInfos)
        {
            variableFieldInfo.read(byteArray, instance);
        }
        return instance;
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

        void read(InternalByteArray byteArray, Object instance)
        {
            switch (classId)
            {
                case ReflectUtil.PRIMITIVE_INT -> accessor.set(instance, byteArray.readVarInt());
                case ReflectUtil.PRIMITIVE_LONG -> accessor.set(instance, byteArray.readVarLong());
                case ReflectUtil.PRIMITIVE_FLOAT -> accessor.set(instance, byteArray.readFloat());
                case ReflectUtil.PRIMITIVE_DOUBLE -> accessor.set(instance, byteArray.readDouble());
                case ReflectUtil.PRIMITIVE_BOOL -> accessor.set(instance, byteArray.readBoolean() );
                case ReflectUtil.PRIMITIVE_CHAR -> accessor.set(instance, byteArray.readChar());
                case ReflectUtil.PRIMITIVE_SHORT -> accessor.set(instance, byteArray.readShort());
                case ReflectUtil.PRIMITIVE_BYTE -> accessor.set(instance, byteArray.get());
                case ReflectUtil.CLASS_INT ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readVarInt());
                    }
                }
                case ReflectUtil.CLASS_LONG ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readVarLong());
                    }
                }
                case ReflectUtil.CLASS_FLOAT ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readFloat());
                    }
                }
                case ReflectUtil.CLASS_DOUBLE ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readDouble());
                    }
                }
                case ReflectUtil.CLASS_BOOL ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readBoolean());
                    }
                }
                case ReflectUtil.CLASS_CHAR ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readChar());
                    }
                }
                case ReflectUtil.CLASS_SHORT ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.readShort());
                    }
                }
                case ReflectUtil.CLASS_BYTE ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.set(instance, byteArray.get());
                    }
                }
                case ReflectUtil.CLASS_STRING ->
                {
                    if (byteArray.get() == JfireSE.NULL)
                    {
                        accessor.setObject(instance, null);
                    }
                    else
                    {
                        accessor.setObject(instance, byteArray.readString());
                    }
                }
            }
        }

        void write(InternalByteArray byteArray, Object instance)
        {
            switch (classId)
            {
                case ReflectUtil.PRIMITIVE_INT -> byteArray.writeVarInt(accessor.getInt(instance));
                case ReflectUtil.PRIMITIVE_LONG -> byteArray.writeVarLong(accessor.getLong(instance));
                case ReflectUtil.PRIMITIVE_FLOAT -> byteArray.writeFloat(accessor.getFloat(instance));
                case ReflectUtil.PRIMITIVE_DOUBLE -> byteArray.writeDouble(accessor.getDouble(instance));
                case ReflectUtil.PRIMITIVE_BOOL -> byteArray.writeBoolean(accessor.getBoolean(instance));
                case ReflectUtil.PRIMITIVE_CHAR -> byteArray.writeChar(accessor.getChar(instance));
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
                        byteArray.put((byte) (value ? 1 : 0));
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
                        byteArray.put(JfireSE.NOT_NULL);
                        byteArray.writeChar(value);
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
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
                        byteArray.put(JfireSE.NOT_NULL);
                        byteArray.writeString(value);
                    }
                }
                default -> throw new IllegalArgumentException();
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
                classInfo.writeBytes(byteArray, value, true);
            }
        }

        public void read(InternalByteArray byteArray, Object instance)
        {
            byte flag = byteArray.get();
            switch (flag)
            {
                case JfireSE.NULL -> accessor.setObject(instance, null);
                case 7 -> accessor.setObject(instance, classInfo.readBytes(byteArray, true));
                case 8 -> accessor.setObject(instance, classInfo.readBytes(byteArray, false));
                case 9 -> accessor.setObject(instance, classInfo.getTracking(byteArray.readVarInt()));
                default -> throw new IllegalArgumentException();
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

        void write(InternalByteArray byteArray, Object instance)
        {
            if (instance == null)
            {
                byteArray.put(JfireSE.NULL);
                return;
            }
            Class<?> clazz = instance.getClass();
            if (classInfo != null && classInfo.getClazz() == clazz)
            {
                classInfo.writeBytes(byteArray, instance, false);
            }
            else
            {
                classInfo = jfireSE.getClassInfo(clazz);
                classInfo.writeBytes(byteArray, instance, false);
            }
        }

        void read(InternalByteArray byteArray, Object instance)
        {
            accessor.setObject(instance, jfireSE.readBytes(byteArray));
        }
    }


}
