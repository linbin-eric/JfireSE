package cc.jfire.se2.serializer.impl.ObjectSerializer;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.se2.ByteArray;

import java.lang.reflect.Field;

public class PrimitiveFieldInfo extends FieldInfo
{
    public PrimitiveFieldInfo(Field field)
    {
        super(field);
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

    @Override
    public void read(ByteArray byteArray, Object instance)
    {
        switch (classId)
        {
            case ReflectUtil.PRIMITIVE_BYTE -> accessor.set(instance, byteArray.get());
            case ReflectUtil.PRIMITIVE_INT -> accessor.set(instance, byteArray.readVarInt());
            case ReflectUtil.PRIMITIVE_SHORT -> accessor.set(instance, (short) byteArray.readVarInt());
            case ReflectUtil.PRIMITIVE_LONG -> accessor.set(instance, byteArray.readVarLong());
            case ReflectUtil.PRIMITIVE_FLOAT -> accessor.set(instance, byteArray.readFloat());
            case ReflectUtil.PRIMITIVE_DOUBLE -> accessor.set(instance, byteArray.readDouble());
            case ReflectUtil.PRIMITIVE_BOOL -> accessor.set(instance, byteArray.readBoolean());
            case ReflectUtil.PRIMITIVE_CHAR -> accessor.set(instance, byteArray.readChar());
            default -> throw new RuntimeException("不支持的类型");
        }
    }
}
