package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

public abstract class BoxedArraySerializer<T> implements Serializer
{
    public static class IntegerArraySerializer extends BoxedArraySerializer<Integer[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Integer[] arr = (Integer[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Integer each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarInt(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int       length = byteArray.readPositiveVarInt();
            Integer[] arr    = new Integer[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readVarInt();
                }
            }
            return arr;
        }
    }

    public static class LongArraySerializer extends BoxedArraySerializer<Long[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Long[] arr = (Long[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Long each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarLong(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int    length = byteArray.readPositiveVarInt();
            Long[] arr    = new Long[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readVarLong();
                }
            }
            return arr;
        }
    }

    public static class FloatArraySerializer extends BoxedArraySerializer<Float[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Float[] arr = (Float[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Float each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeFloat(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int     length = byteArray.readPositiveVarInt();
            Float[] arr    = new Float[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readFloat();
                }
            }
            return arr;
        }
    }

    public static class DoubleArraySerializer extends BoxedArraySerializer<Double[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Double[] arr = (Double[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Double each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeDouble(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int      length = byteArray.readPositiveVarInt();
            Double[] arr    = new Double[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readDouble();
                }
            }
            return arr;
        }
    }

    public static class ByteArraySerializer extends BoxedArraySerializer<Byte[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Byte[] arr = (Byte[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Byte each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.put(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int    length = byteArray.readPositiveVarInt();
            Byte[] arr    = new Byte[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.get();
                }
            }
            return arr;
        }
    }

    public static class BooleanArraySerializer extends BoxedArraySerializer<Boolean[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Boolean[] arr = (Boolean[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Boolean each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeBoolean(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int       length = byteArray.readPositiveVarInt();
            Boolean[] arr    = new Boolean[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readBoolean();
                }
            }
            return arr;
        }
    }

    public static class CharArraySerializer extends BoxedArraySerializer<Character[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Character[] arr = (Character[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Character each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeChar(each);
                }
            }
        }
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        int         length = byteArray.readPositiveVarInt();
        Character[] arr    = new Character[length];
        if (refTracking != null)
        {
            refTracking.addTracking(arr);
        }
        for (int i = 0; i < arr.length; i++)
        {
            if (byteArray.get() == JfireSE.NULL)
            {
                arr[i] = null;
            }
            else
            {
                arr[i] = byteArray.readChar();
            }
        }
        return arr;
    }

    public static class ShortArraySerializer extends PrimitiveArraySerializer<Short[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            Short[] arr = (Short[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                Short each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeShort(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int     length = byteArray.readPositiveVarInt();
            Short[] arr    = new Short[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readShort();
                }
            }
            return arr;
        }
    }
    public static  class  StringArraySerializer extends BoxedArraySerializer<String[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            String[] arr = (String[]) instance;
            byteArray.writePositiveVarInt(arr.length);
            for (int i = 0; i < arr.length; i++)
            {
                String each = arr[i];
                if (each == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeString(each);
                }
            }
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int      length = byteArray.readPositiveVarInt();
            String[] arr    = new String[length];
            if (refTracking != null)
            {
                refTracking.addTracking(arr);
            }
            for (int i = 0; i < arr.length; i++)
            {
                if (byteArray.get() == JfireSE.NULL)
                {
                    arr[i] = null;
                }
                else
                {
                    arr[i] = byteArray.readString();
                }
            }
            return arr;
        }
    }
}
