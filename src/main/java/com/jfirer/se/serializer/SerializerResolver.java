package com.jfirer.se.serializer;

import com.jfirer.se.InternalByteArray;
import com.jfirer.se.JfireSE;
import com.jfirer.se.serializer.impl.ArraySerializer;
import com.jfirer.se.serializer.impl.ObjectSerializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.util.IdentityHashMap;
import java.util.Map;

public class SerializerResolver
{
    private              Map<Class, Serializer> store  = new IdentityHashMap<>();
    private static final Unsafe                 UNSAFE = Unsafe.getUnsafe();

    public SerializerResolver()
    {
        store.put(int[].class, new IntArraySerializer());
        store.put(long[].class, new LongArraySerializer());
        store.put(double[].class, new DoubleArraySerializer());
        store.put(float[].class, new FloatArraySerializer());
        store.put(short[].class, new ShortArraySerializer());
        store.put(char[].class, new CharArraySerializer());
        store.put(byte[].class, new ByteArraySerializer());
        store.put(boolean[].class, new BooleanArraySerializer());
        store.put(Integer[].class, new BoxedIntArraySerializer());
        store.put(Long[].class, new BoxedLongArraySerializer());
        store.put(Double[].class, new BoxedDoubleArraySerializer());
        store.put(Float[].class, new BoxedFloatArraySerializer());
        store.put(Short[].class, new BoxedShortArraySerializer());
        store.put(Character[].class, new BoxedCharArraySerializer());
        store.put(Byte[].class, new BoxedByteArraySerializer());
        store.put(Boolean[].class, new BoxedBooleanArraySerializer());
        store.put(String[].class, new StringArraySerializer());
    }

    public Serializer getSerializer(Class clazz, JfireSE jfireSE)
    {
        Serializer serializer = store.get(clazz);
        if (serializer != null)
        {
            return serializer;
        }
        if (clazz.isArray())
        {
            serializer = new ArraySerializer(clazz, jfireSE);
        }
        else
        {
            serializer = new ObjectSerializer(clazz, jfireSE);
        }
        store.put(clazz, serializer);
        return serializer;
    }

    public void registerSerializer(Class clazz, Serializer serializer)
    {
        store.put(clazz, serializer);
    }

    private static int getShift(int value)
    {
        int count = 0;
        while (value != 0)
        {
            count++;
            value >>= 1;
        }
        return count-1;
    }

    class IntArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(int[].class);
        int  shift  = getShift(UNSAFE.arrayIndexScale(int[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            int[] array = (int[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeVarInt(UNSAFE.getInt(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int   length = byteArray.readVarInt();
            int[] array  = new int[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putInt(array, offset + ((long) i << shift), byteArray.readVarInt());
            }
            return array;
        }
    }

    class LongArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(long[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(long[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            long[] array = (long[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeVarLong(UNSAFE.getLong(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int    length = byteArray.readVarInt();
            long[] array  = new long[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putLong(array, offset + ((long) i << shift), byteArray.readVarLong());
            }
            return array;
        }
    }

    class DoubleArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(double[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(double[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            double[] array = (double[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeDouble(UNSAFE.getDouble(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int      length = byteArray.readVarInt();
            double[] array  = new double[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putDouble(array, offset + ((long) i << shift), byteArray.readDouble());
            }
            return array;
        }
    }

    class FloatArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(float[].class);
        int  shift  = getShift(UNSAFE.arrayIndexScale(float[].class) );

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            float[] array = (float[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeFloat(UNSAFE.getFloat(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int     length = byteArray.readVarInt();
            float[] array  = new float[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putFloat(array, offset + ((long) i << shift), byteArray.readFloat());
            }
            return array;
        }
    }

    class CharArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(char[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(char[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            char[] array = (char[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeVarChar(UNSAFE.getChar(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int    length = byteArray.readVarInt();
            char[] array  = new char[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putChar(array, offset + ((long) i << shift), byteArray.readVarChar());
            }
            return array;
        }
    }

    class ShortArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(short[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(short[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            short[] array = (short[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.writeShort(UNSAFE.getShort(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int     length = byteArray.readVarInt();
            short[] array  = new short[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putShort(array, offset + ((long) i << shift), byteArray.readShort());
            }
            return array;
        }
    }

    class ByteArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(byte[].class);
        int  shift  = getShift(UNSAFE.arrayIndexScale(byte[].class) );

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            byte[] array = (byte[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.put(UNSAFE.getByte(array, offset + ((long) i << shift)));
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int    length = byteArray.readVarInt();
            byte[] array  = new byte[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putByte(array, offset + ((long) i << shift), byteArray.get());
            }
            return array;
        }
    }

    class BooleanArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(boolean[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(boolean[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            boolean[] array = (boolean[]) instance;
            byteArray.writeVarInt(array.length);
            for (int i = 0; i < array.length; i++)
            {
                byteArray.put(UNSAFE.getBoolean(array, offset + ((long) i << shift)) ? (byte) 1 : (byte) 0);
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int       length = byteArray.readVarInt();
            boolean[] array  = new boolean[length];
            for (int i = 0; i < length; i++)
            {
                UNSAFE.putBoolean(array, offset + ((long) i << shift), byteArray.get() == 1);
            }
            return array;
        }
    }

    class BoxedIntArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Integer[].class);
        int  shift  = getShift(UNSAFE.arrayIndexScale(Integer[].class) );

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Integer[] array = (Integer[]) instance;
            byteArray.writeVarInt(array.length);
            for (Integer i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarInt(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int       length = byteArray.readVarInt();
            Integer[] array  = new Integer[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readVarInt());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedLongArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Long[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Long[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Long[] array = (Long[]) instance;
            byteArray.writeVarInt(array.length);
            for (Long i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarLong(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int    length = byteArray.readVarInt();
            Long[] array  = new Long[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readVarLong());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedDoubleArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Double[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Double[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Double[] array = (Double[]) instance;
            byteArray.writeVarInt(array.length);
            for (Double i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeDouble(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int      length = byteArray.readVarInt();
            Double[] array  = new Double[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readDouble());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedFloatArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Float[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Float[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Float[] array = (Float[]) instance;
            byteArray.writeVarInt(array.length);
            for (Float i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeFloat(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int     length = byteArray.readVarInt();
            Float[] array  = new Float[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readFloat());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedBooleanArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Boolean[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Boolean[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Boolean[] array = (Boolean[]) instance;
            byteArray.writeVarInt(array.length);
            for (Boolean i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.put(i ? (byte) 1 : 0);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int       length = byteArray.readVarInt();
            Boolean[] array  = new Boolean[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.get() == 1);
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedCharArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Character[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Character[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Character[] array = (Character[]) instance;
            byteArray.writeVarInt(array.length);
            for (Character i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeVarChar(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int         length = byteArray.readVarInt();
            Character[] array  = new Character[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readVarChar());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedByteArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Byte[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(Byte[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Byte[] array = (Byte[]) instance;
            byteArray.writeVarInt(array.length);
            for (Byte i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.put(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int    length = byteArray.readVarInt();
            Byte[] array  = new Byte[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.get());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class BoxedShortArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(Short[].class);
        int  shift  = getShift(UNSAFE.arrayIndexScale(Short[].class) );

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            Short[] array = (Short[]) instance;
            byteArray.writeVarInt(array.length);
            for (Short i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeShort(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int     length = byteArray.readVarInt();
            Short[] array  = new Short[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readShort());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }

    class StringArraySerializer implements Serializer
    {
        long offset = UNSAFE.arrayBaseOffset(String[].class);
        int  shift  =  getShift(UNSAFE.arrayIndexScale(String[].class));

        @Override
        public void writeBytes(InternalByteArray byteArray, Object instance)
        {
            String[] array = (String[]) instance;
            byteArray.writeVarInt(array.length);
            for (String i : array)
            {
                if (i == null)
                {
                    byteArray.put(JfireSE.NULL);
                }
                else
                {
                    byteArray.put(JfireSE.NOT_NULL);
                    byteArray.writeString(i);
                }
            }
        }

        @Override
        public Object readBytes(InternalByteArray byteArray)
        {
            int      length = byteArray.readVarInt();
            String[] array  = new String[length];
            for (int i = 0; i < length; i++)
            {
                if (byteArray.get() == JfireSE.NOT_NULL)
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), byteArray.readString());
                }
                else
                {
                    UNSAFE.putReference(array, offset + ((long) i << shift), null);
                }
            }
            return array;
        }
    }
}
