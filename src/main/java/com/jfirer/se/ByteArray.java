package com.jfirer.se;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteOrder;

public class ByteArray
{
    protected static final boolean LITTLE_ENDIAN         = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
    protected static final Unsafe  UNSAFE                = Unsafe.getUnsafe();
    public static final    long    UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    public static final    int     BOOLEAN_ARRAY_OFFSET;
    public static final    int     BYTE_ARRAY_OFFSET;
    public static final    int     CHAR_ARRAY_OFFSET;
    public static final    int     SHORT_ARRAY_OFFSET;
    public static final    int     INT_ARRAY_OFFSET;
    public static final    int     LONG_ARRAY_OFFSET;
    public static final    int     FLOAT_ARRAY_OFFSET;
    public static final    int     DOUBLE_ARRAY_OFFSET;
    public static final    boolean STRING_VALUE_FIELD_IS_CHARS;
    public static final    boolean STRING_VALUE_FIELD_IS_BYTES;
    public static final    long    STRING_VALUE_FIELD_OFFSET;
    public static final    long    STRING_CODER_FIELD_OFFSET;
    protected              byte[]  array;
    protected              int     writerIndex           = 0;
    protected              int     readerIndex           = 0;
    protected              boolean needCheck             = true;

    static
    {
        BOOLEAN_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(boolean[].class);
        BYTE_ARRAY_OFFSET    = UNSAFE.arrayBaseOffset(byte[].class);
        CHAR_ARRAY_OFFSET    = UNSAFE.arrayBaseOffset(char[].class);
        SHORT_ARRAY_OFFSET   = UNSAFE.arrayBaseOffset(short[].class);
        INT_ARRAY_OFFSET     = UNSAFE.arrayBaseOffset(int[].class);
        LONG_ARRAY_OFFSET    = UNSAFE.arrayBaseOffset(long[].class);
        FLOAT_ARRAY_OFFSET   = UNSAFE.arrayBaseOffset(float[].class);
        DOUBLE_ARRAY_OFFSET  = UNSAFE.arrayBaseOffset(double[].class);
        try
        {
            Field field = String.class.getDeclaredField("value");
            STRING_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(field);
            // Java8 string
            STRING_VALUE_FIELD_IS_CHARS = field != null && field.getType() == char[].class;
            // Java11 string
            STRING_VALUE_FIELD_IS_BYTES = field != null && field.getType() == byte[].class;
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        long stringCoderFieldOffset1;
        try
        {
            Field field = String.class.getDeclaredField("coder");
            stringCoderFieldOffset1 = UNSAFE.objectFieldOffset(field);
        }
        catch (NoSuchFieldException e)
        {
            stringCoderFieldOffset1 = 0;
        }
        STRING_CODER_FIELD_OFFSET = stringCoderFieldOffset1;
    }

    protected ByteArray(int size)
    {
        array = new byte[size];
    }

    protected ByteArray(byte[] array)
    {
        this.array  = array;
        writerIndex = array.length;
    }

    public static ByteArray allocate(int size)
    {
        return new InternalByteArray(size);
    }

    public static ByteArray allocate()
    {
        return new InternalByteArray(1024);
    }

    public static ByteArray wrap(byte[] array)
    {
        return new InternalByteArray(array);
    }

    public void setNeedCheck(boolean needCheck)
    {
        this.needCheck = needCheck;
    }

    protected void ensureNewWriterIndex(int newWriterIndex)
    {
        if (newWriterIndex > array.length)
        {
            int    newLen = newWriterIndex > (array.length << 1) ? newWriterIndex : (array.length << 1);
            byte[] tmp    = new byte[newLen];
            System.arraycopy(array, 0, tmp, 0, array.length);
            array = tmp;
        }
    }

    public void clear()
    {
        writerIndex = readerIndex = 0;
    }

    public void put(byte value)
    {
        int writerIdx      = writerIndex;
        int newWriterIndex = writerIdx + 1;
        ensureNewWriterIndex(newWriterIndex);
        UNSAFE.putByte(array, BYTE_ARRAY_OFFSET + writerIdx, value);
        writerIndex = newWriterIndex;
    }

    public byte get()
    {
        int readerIdx = readerIndex;
        if (readerIdx < writerIndex)
        {
            byte result = UNSAFE.getByte(array, BYTE_ARRAY_OFFSET + readerIdx);
            readerIndex = readerIdx + 1;
            return result;
        }
        else
        {
            throw new IllegalArgumentException("读取的内容不足");
        }
//        byte result = array[readIndex];
//        readIndex += 1;
//        return result;
    }

    public byte[] toArray()
    {
        byte[] result = new byte[writerIndex];
        System.arraycopy(array, 0, result, 0, writerIndex);
        return result;
    }

    public int getWriterIndex()
    {
        return writerIndex;
    }

    public void setWriterIndex(int writerIndex)
    {
        this.writerIndex = writerIndex;
    }

    public void setReadPosi(int readPosi)
    {
        this.readerIndex = readPosi;
    }
}
