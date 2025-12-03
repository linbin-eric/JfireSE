package cc.jfire.se2.serializer.impl;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

public abstract class BoxedTypeSerializer implements Serializer
{
    public static class StringSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeString((String) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readString();
        }
    }

    public static class IntegerSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeVarInt((Integer) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readVarInt();
        }
    }

    public static class ShortSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeVarInt((Short) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return Short.valueOf((short) byteArray.readVarInt());
        }
    }
    public  static  class ByteSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.put((Byte) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.get();
        }
    }
    public static class LongSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeVarLong((Long) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readVarLong();
        }
    }
    public static class FloatSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeFloat((Float) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readFloat();
        }
    }
    public static class DoubleSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeDouble((Double) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readDouble();
        }
    }
    public static class BooleanSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeBoolean((Boolean) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readBoolean();
        }
    }
    public static class CharacterSerializer extends BoxedTypeSerializer
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byteArray.writeChar((Character) instance);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            return byteArray.readChar();
        }
    }
}
