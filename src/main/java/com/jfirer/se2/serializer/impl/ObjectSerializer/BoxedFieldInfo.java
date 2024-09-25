package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;

import java.lang.reflect.Field;

public class BoxedFieldInfo extends FieldInfo
{
    public BoxedFieldInfo(Field field)
    {
        super(field);
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        switch (classId)
        {
            case ReflectUtil.CLASS_BOOL ->
            {
                Boolean obj = (Boolean) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeBoolean(obj);
                }
            }
            case ReflectUtil.CLASS_BYTE ->
            {
                Byte obj = (Byte) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.put(obj);
                }
            }
            case ReflectUtil.CLASS_CHAR ->
            {
                Character obj = (Character) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeChar(obj);
                }
            }
            case ReflectUtil.CLASS_SHORT ->
            {
                Short obj = (Short) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarInt(obj);
                }
            }
            case ReflectUtil.CLASS_INT ->
            {
                Integer obj = (Integer) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarInt(obj);
                }
            }
            case ReflectUtil.CLASS_LONG ->
            {
                Long obj = (Long) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarLong(obj);
                }
            }
            case ReflectUtil.CLASS_FLOAT ->
            {
                Float obj = (Float) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeFloat(obj);
                }
            }
            case ReflectUtil.CLASS_DOUBLE ->
            {
                Double obj = (Double) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeDouble(obj);
                }
            }
            case ReflectUtil.CLASS_STRING ->
            {
                String obj = (String) accessor.getReference(instance);
                if (obj == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeString(obj);
                }
            }
            default -> throw new RuntimeException("不支持的类型");
        }
    }

    @Override
    public void read(ByteArray byteArray, Object instance)
    {
        boolean exist = byteArray.get() == JfireSE.NOT_NULL;
        switch (classId)
        {
            case ReflectUtil.CLASS_BOOL ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readBoolean());
                }
            }
            case ReflectUtil.CLASS_BYTE ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.get());
                }
            }
            case ReflectUtil.CLASS_CHAR ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readChar());
                }
            }
            case ReflectUtil.CLASS_SHORT ->
            {
                if (exist)
                {
                    accessor.setReference(instance, (short) byteArray.readVarInt());
                }
            }
            case ReflectUtil.CLASS_INT ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readVarInt());
                }
            }
            case ReflectUtil.CLASS_LONG ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readVarLong());
                }
            }
            case ReflectUtil.CLASS_FLOAT ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readFloat());
                }
            }
            case ReflectUtil.CLASS_DOUBLE ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readDouble());
                }
            }
            case ReflectUtil.CLASS_STRING ->
            {
                if (exist)
                {
                    accessor.setReference(instance, byteArray.readString());
                }
            }
            default -> throw new RuntimeException("不支持的类型");
        }
    }
}
