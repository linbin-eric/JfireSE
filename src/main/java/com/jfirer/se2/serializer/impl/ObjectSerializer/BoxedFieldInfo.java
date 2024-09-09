package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;

import java.lang.reflect.Field;

public class BoxedFieldInfo extends FieldInfo
{
    public BoxedFieldInfo(Field field)
    {
        super(ReflectUtil.getClassId(field.getType()), new ValueAccessor(field));
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        switch (classId)
        {
            case ReflectUtil.CLASS_BOOL ->
            {
                Boolean obj = accessor.getBooleanObject(instance);
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
                Byte obj = accessor.getByteObject(instance);
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
                Character obj = accessor.getCharObject(instance);
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
                Short obj = accessor.getShortObject(instance);
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
                Integer obj = accessor.getIntObject(instance);
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
                Long obj = accessor.getLongObject(instance);
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
                Float obj = accessor.getFloatObject(instance);
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
                Double obj = accessor.getDoubleObject(instance);
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
                String obj = (String) accessor.get(instance);
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
}
