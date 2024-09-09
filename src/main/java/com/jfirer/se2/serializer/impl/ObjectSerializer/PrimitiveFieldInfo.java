package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se2.ByteArray;

import java.lang.reflect.Field;

public class PrimitiveFieldInfo extends FieldInfo
{
    public PrimitiveFieldInfo(Field field)
    {
        super(ReflectUtil.getClassId(field.getType()), new ValueAccessor(field));
    }

    public void write(ByteArray byteArray, Object instance)
    {
        switch (classId)
        {
            case ReflectUtil.PRIMITIVE_BYTE -> byteArray.put(accessor.getByte(instance));
            case ReflectUtil.PRIMITIVE_INT -> byteArray.writeVarInt(accessor.getInt(instance));
            case ReflectUtil.PRIMITIVE_SHORT -> byteArray.writeVarInt(accessor.getShort(instance));
            case ReflectUtil.PRIMITIVE_LONG -> byteArray.writeVarLong(accessor.getLong(instance));
            case ReflectUtil.PRIMITIVE_FLOAT -> byteArray.writeFloat(accessor.getFloat(instance));
            case ReflectUtil.PRIMITIVE_DOUBLE -> byteArray.writeDouble(accessor.getDouble(instance));
            case ReflectUtil.PRIMITIVE_BOOL -> byteArray.writeBoolean(accessor.getBoolean(instance));
            case ReflectUtil.PRIMITIVE_CHAR -> byteArray.writeChar(accessor.getChar(instance));
            default -> throw new RuntimeException("不支持的类型");
        }
    }
}
