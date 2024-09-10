package com.jfirer.se2;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.fse.InternalByteArray;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;
import java.util.function.BiFunction;

/**
 * 对于int，long，short，char，在内存中需要统一一种存储顺序，才能跨机器传输与识别。
 * 由于存在 varInt 和 varLong 的变长编码，因此采用小端序，能够较好的适应这种变长编码。
 */
public class ByteArray
{
    protected static final boolean                          LITTLE_ENDIAN         = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN);
    protected static final Unsafe                           UNSAFE                = Unsafe.getUnsafe();
    public static final    long                             UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
    public static final    int                              BOOLEAN_ARRAY_OFFSET;
    public static final    int                              BYTE_ARRAY_OFFSET;
    public static final    int                              CHAR_ARRAY_OFFSET;
    public static final    int                              SHORT_ARRAY_OFFSET;
    public static final    int                              INT_ARRAY_OFFSET;
    public static final    int                              LONG_ARRAY_OFFSET;
    public static final    int                              FLOAT_ARRAY_OFFSET;
    public static final    int                              DOUBLE_ARRAY_OFFSET;
    public static final    long                             STRING_VALUE_FIELD_OFFSET;
    public static final    long                             STRING_CODER_FIELD_OFFSET;
    protected              byte[]                           array;
    protected              int                              writerIndex           = 0;
    protected              int                              readerIndex           = 0;
    protected              boolean                          needCheck             = true;
    protected static final byte                             LATIN1                = 0;
    protected static final byte                             LATIN1_BOXED          = LATIN1;
    protected static final byte                             UTF16                 = 1;
    protected static final byte                             UTF16_BOXED           = UTF16;
    protected static final BiFunction<byte[], Byte, String> PRIVATE_STRING_CONSTRUCTOR;

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
            STRING_VALUE_FIELD_OFFSET = UNSAFE.objectFieldOffset(String.class.getDeclaredField("value"));
            STRING_CODER_FIELD_OFFSET = UNSAFE.objectFieldOffset(String.class.getDeclaredField("coder"));
            MethodHandles.Lookup in     = ReflectUtil.TRUSTED_LOOKUP.in(String.class);
            MethodHandle         handle = in.findConstructor(String.class, MethodType.methodType(void.class, byte[].class, byte.class));
            PRIVATE_STRING_CONSTRUCTOR = (BiFunction<byte[], Byte, String>) LambdaMetafactory.metafactory(in, "apply",//
                                                                                                          MethodType.methodType(BiFunction.class),//
                                                                                                          handle.type().generic(),//
                                                                                                          handle,//
                                                                                                          MethodType.methodType(String.class, byte[].class, Byte.class)//
            ).getTarget().invokeExact();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public ByteArray(int size)
    {
        array = new byte[size];
    }

    public ByteArray(byte[] array)
    {
        this.array  = array;
        writerIndex = array.length;
    }

    public static com.jfirer.fse.ByteArray allocate(int size)
    {
        return new InternalByteArray(size);
    }

    public static com.jfirer.fse.ByteArray allocate()
    {
        return new InternalByteArray(1024);
    }

    public static com.jfirer.fse.ByteArray wrap(byte[] array)
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

    public int writeVarInt(int v)
    {
        ensureNewWriterIndex(writerIndex + 8);
        return unsafeWriteVarInt(v);
    }

    /**
     * Writes a 1-5 byte int.
     *
     * @return The number of bytes written.
     */
    public int writePositiveVarInt(int v)
    {
        ensureNewWriterIndex(writerIndex + 8);
        return writePositiveVarIntWithoutEnsure(v);
    }

    /**
     * For implementation efficiency, this method needs at most 8 bytes for writing 5 bytes using long
     * to avoid using two memory operations.
     */
    private int unsafeWriteVarInt(int v)
    {
        // Ensure negatives close to zero is encode in little bytes.
        v = (v << 1) ^ (v >> 31);
        return writePositiveVarIntWithoutEnsure(v);
    }

    private int writePositiveVarIntWithoutEnsure(int v)
    {
        // The encoding algorithm are based on kryo UnsafeMemoryOutput.writeVarInt
        // varint are written using little endian byte order.
        // This version should have better performance since it remove an index update.
        long      value       = v;
        final int writerIndex = this.writerIndex;
        long      varInt      = (value & 0x7F);
        value >>>= 7;
        if (value == 0)
        {
            UNSAFE.putByte(array, BYTE_ARRAY_OFFSET + writerIndex, (byte) varInt);
            this.writerIndex = writerIndex + 1;
            return 1;
        }
        // bit 8 `set` indicates have next data bytes.
        varInt |= 0x80;
        varInt |= ((value & 0x7F) << 8);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, (int) varInt);
            this.writerIndex = writerIndex + 2;
            return 2;
        }
        varInt |= (0x80 << 8);
        varInt |= ((value & 0x7F) << 16);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, (int) varInt);
            this.writerIndex = writerIndex + 3;
            return 3;
        }
        varInt |= (0x80 << 16);
        varInt |= ((value & 0x7F) << 24);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, (int) varInt);
            this.writerIndex = writerIndex + 4;
            return 4;
        }
        varInt |= (0x80L << 24);
        varInt |= ((value & 0x7F) << 32);
        varInt &= 0xFFFFFFFFFL;
        unsafePutLong(writerIndex, varInt);
        this.writerIndex = writerIndex + 5;
        return 5;
    }

    public void unsafePutInt(int index, int value)
    {
        final long pos = BYTE_ARRAY_OFFSET + index;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putInt(array, pos, value);
        }
        else
        {
            UNSAFE.putInt(array, pos, Integer.reverseBytes(value));
        }
    }

    public void unsafePutLong(int index, long value)
    {
        final long pos = BYTE_ARRAY_OFFSET + index;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putLong(array, pos, value);
        }
        else
        {
            UNSAFE.putLong(array, pos, Long.reverseBytes(value));
        }
    }

    public int readVarInt()
    {
        int r = readPositiveVarInt();
        return (r >>> 1) ^ -(r & 1);
    }

    public int readPositiveVarInt()
    {
        int readIdx = readerIndex;
        if (writerIndex - readIdx < 5)
        {
            return readPositiveVarIntSlow();
        }
        // varint are written using little endian byte order, so read by little endian byte order.
        int fourByteValue = unsafeGetInt(readIdx);
        int b             = fourByteValue & 0xFF;
        readIdx++; // read one byte
        int result = b & 0x7F;
        if ((b & 0x80) != 0)
        {
            readIdx++; // read one byte
            b = (fourByteValue >>> 8) & 0xFF;
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0)
            {
                readIdx++; // read one byte
                b = (fourByteValue >>> 16) & 0xFF;
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0)
                {
                    readIdx++; // read one byte
                    b = (fourByteValue >>> 24) & 0xFF;
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0)
                    {
                        b = unsafeGet(readIdx++); // read one byte
                        result |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        readerIndex = readIdx;
        return result;
    }

    private byte unsafeGet(int index)
    {
        final long pos = BYTE_ARRAY_OFFSET + index;
        return UNSAFE.getByte(array, pos);
    }

    private int unsafeGetInt(int index)
    {
        final long pos = BYTE_ARRAY_OFFSET + index;
        if (LITTLE_ENDIAN)
        {
            return UNSAFE.getInt(array, pos);
        }
        else
        {
            return Integer.reverseBytes(UNSAFE.getInt(array, pos));
        }
    }

    private int readPositiveVarIntSlow()
    {
        int b      = get();
        int result = b & 0x7F;
        if ((b & 0x80) != 0)
        {
            b = get();
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0)
            {
                b = get();
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0)
                {
                    b = get();
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0)
                    {
                        b = get();
                        result |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        return result;
    }

    public int writeVarLong(long value)
    {
        ensureNewWriterIndex(writerIndex + 9);
        value = (value << 1) ^ (value >> 63);
        return unsafeWritePositiveVarLong(value);
    }

    public int writePositiveVarLong(long value)
    {
        // Var long encoding algorithm is based kryo UnsafeMemoryOutput.writeVarLong.
        // var long are written using little endian byte order.
        ensureNewWriterIndex(writerIndex + 9);
        return unsafeWritePositiveVarLong(value);
    }

    public int unsafeWritePositiveVarLong(long value)
    {
        final int writerIndex = this.writerIndex;
        int       varInt;
        varInt = (int) (value & 0x7F);
        value >>>= 7;
        if (value == 0)
        {
            UNSAFE.putByte(array, BYTE_ARRAY_OFFSET + writerIndex, (byte) varInt);
            this.writerIndex = writerIndex + 1;
            return 1;
        }
        varInt |= 0x80;
        varInt |= ((value & 0x7F) << 8);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, varInt);
            this.writerIndex = writerIndex + 2;
            return 2;
        }
        varInt |= (0x80 << 8);
        varInt |= ((value & 0x7F) << 16);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, varInt);
            this.writerIndex = writerIndex + 3;
            return 3;
        }
        varInt |= (0x80 << 16);
        varInt |= ((value & 0x7F) << 24);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutInt(writerIndex, varInt);
            this.writerIndex = writerIndex + 4;
            return 4;
        }
        varInt |= (0x80L << 24);
        long varLong = (varInt & 0xFFFFFFFFL);
        varLong |= ((value & 0x7F) << 32);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutLong(writerIndex, varLong);
            this.writerIndex = writerIndex + 5;
            return 5;
        }
        varLong |= (0x80L << 32);
        varLong |= ((value & 0x7F) << 40);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutLong(writerIndex, varLong);
            this.writerIndex = writerIndex + 6;
            return 6;
        }
        varLong |= (0x80L << 40);
        varLong |= ((value & 0x7F) << 48);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutLong(writerIndex, varLong);
            this.writerIndex = writerIndex + 7;
            return 7;
        }
        varLong |= (0x80L << 48);
        varLong |= ((value & 0x7F) << 56);
        value >>>= 7;
        if (value == 0)
        {
            unsafePutLong(writerIndex, varLong);
            this.writerIndex = writerIndex + 8;
            return 8;
        }
        varLong |= (0x80L << 56);
        unsafePutLong(writerIndex, varLong);
        UNSAFE.putByte(array, BYTE_ARRAY_OFFSET + writerIndex + 8, (byte) (value & 0xFF));
        this.writerIndex = writerIndex + 9;
        return 9;
    }

    public long readVarLong()
    {
        long result = readPositiveVarLong();
        return ((result >>> 1) ^ -(result & 1));
    }

    public long readPositiveVarLong()
    {
        int readIdx = readerIndex;
        if (writerIndex - readIdx < 9)
        {
            return readPositiveVarLongSlow();
        }
        // varint are written using little endian byte order, so read by little endian byte order.
        long eightByteValue = unsafeGetLong(readIdx);
        long b              = eightByteValue & 0xFF;
        readIdx++; // read one byte
        long result = b & 0x7F;
        if ((b & 0x80) != 0)
        {
            readIdx++; // read one byte
            b = (eightByteValue >>> 8) & 0xFF;
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0)
            {
                readIdx++; // read one byte
                b = (eightByteValue >>> 16) & 0xFF;
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0)
                {
                    readIdx++; // read one byte
                    b = (eightByteValue >>> 24) & 0xFF;
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0)
                    {
                        readIdx++; // read one byte
                        b = (eightByteValue >>> 32) & 0xFF;
                        result |= (b & 0x7F) << 28;
                        if ((b & 0x80) != 0)
                        {
                            readIdx++; // read one byte
                            b = (eightByteValue >>> 40) & 0xFF;
                            result |= (b & 0x7F) << 35;
                            if ((b & 0x80) != 0)
                            {
                                readIdx++; // read one byte
                                b = (eightByteValue >>> 48) & 0xFF;
                                result |= (b & 0x7F) << 42;
                                if ((b & 0x80) != 0)
                                {
                                    readIdx++; // read one byte
                                    b = (eightByteValue >>> 56) & 0xFF;
                                    result |= (b & 0x7F) << 49;
                                    if ((b & 0x80) != 0)
                                    {
                                        b = unsafeGet(readIdx++); // read one byte
                                        result |= b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        readerIndex = readIdx;
        return result;
    }

    private long readPositiveVarLongSlow()
    {
        long b      = get();
        long result = b & 0x7F;
        if ((b & 0x80) != 0)
        {
            b = get();
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0)
            {
                b = get();
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0)
                {
                    b = get();
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0)
                    {
                        b = get();
                        result |= (b & 0x7F) << 28;
                        if ((b & 0x80) != 0)
                        {
                            b = get();
                            result |= (b & 0x7F) << 35;
                            if ((b & 0x80) != 0)
                            {
                                b = get();
                                result |= (b & 0x7F) << 42;
                                if ((b & 0x80) != 0)
                                {
                                    b = get();
                                    result |= (b & 0x7F) << 49;
                                    if ((b & 0x80) != 0)
                                    {
                                        b = get();
                                        // highest bit in last byte is symbols bit.
                                        result |= b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private long unsafeGetLong(int index)
    {
        final long pos = BYTE_ARRAY_OFFSET + index;
        if (LITTLE_ENDIAN)
        {
            return UNSAFE.getLong(array, pos);
        }
        else
        {
            return Long.reverseBytes(UNSAFE.getLong(array, pos));
        }
    }

    public void writeShort(short value)
    {
        final int writerIdx = writerIndex;
        final int newIdx    = writerIdx + 2;
        ensureNewWriterIndex(newIdx);
        final long pos = BYTE_ARRAY_OFFSET + writerIdx;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putShort(array, pos, value);
        }
        else
        {
            UNSAFE.putShort(array, pos, Short.reverseBytes(value));
        }
        writerIndex = newIdx;
    }

    public short readShort()
    {
        int readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - 2)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, 2, writerIndex, this));
        }
        readerIndex = readerIdx + 2;
        final long pos = BYTE_ARRAY_OFFSET + readerIdx;
        if (LITTLE_ENDIAN)
        {
            return UNSAFE.getShort(array, pos);
        }
        else
        {
            return Short.reverseBytes(UNSAFE.getShort(array, pos));
        }
    }

    public void writeBoolean(boolean value)
    {
        final int writerIdx = writerIndex;
        final int newIdx    = writerIdx + 1;
        ensureNewWriterIndex(newIdx);
        final long pos = BYTE_ARRAY_OFFSET + writerIdx;
        UNSAFE.putByte(array, pos, (byte) (value ? 1 : 0));
        writerIndex = newIdx;
    }

    public boolean readBoolean()
    {
        int readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - 1)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, 1, writerIndex, this));
        }
        readerIndex = readerIdx + 1;
        return UNSAFE.getByte(array, BYTE_ARRAY_OFFSET + readerIdx) != 0;
    }

    public void writeChar(char value)
    {
        final int writerIdx = writerIndex;
        final int newIdx    = writerIdx + 2;
        ensureNewWriterIndex(newIdx);
        final long pos = BYTE_ARRAY_OFFSET + writerIdx;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putChar(array, pos, value);
        }
        else
        {
            UNSAFE.putChar(array, pos, Character.reverseBytes(value));
        }
        writerIndex = newIdx;
    }

    public char readChar()
    {
        int readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - 2)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, 2, writerIndex, this));
        }
        readerIndex = readerIdx + 2;
        final long pos = BYTE_ARRAY_OFFSET + readerIdx;
        if (LITTLE_ENDIAN)
        {
            return UNSAFE.getChar(array, pos);
        }
        else
        {
            return Character.reverseBytes(UNSAFE.getChar(array, pos));
        }
    }

    public void writeFloat(float value)
    {
        final int writerIdx = writerIndex;
        final int newIdx    = writerIdx + 4;
        ensureNewWriterIndex(newIdx);
        final long pos = BYTE_ARRAY_OFFSET + writerIdx;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putInt(array, pos, Float.floatToRawIntBits(value));
        }
        else
        {
            UNSAFE.putInt(array, pos, Integer.reverseBytes(Float.floatToRawIntBits(value)));
        }
        writerIndex = newIdx;
    }

    public float readFloat()
    {
        int readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - 4)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, 4, writerIndex, this));
        }
        readerIndex = readerIdx + 4;
        final long pos = BYTE_ARRAY_OFFSET + readerIdx;
        if (LITTLE_ENDIAN)
        {
            return Float.intBitsToFloat(UNSAFE.getInt(array, pos));
        }
        else
        {
            return Float.intBitsToFloat(Integer.reverseBytes(UNSAFE.getInt(array, pos)));
        }
    }

    public void writeDouble(double value)
    {
        final int writerIdx = writerIndex;
        final int newIdx    = writerIdx + 8;
        ensureNewWriterIndex(newIdx);
        final long pos = BYTE_ARRAY_OFFSET + writerIdx;
        if (LITTLE_ENDIAN)
        {
            UNSAFE.putLong(array, pos, Double.doubleToRawLongBits(value));
        }
        else
        {
            UNSAFE.putLong(array, pos, Long.reverseBytes(Double.doubleToRawLongBits(value)));
        }
        writerIndex = newIdx;
    }

    public double readDouble()
    {
        int readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - 8)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, 8, writerIndex, this));
        }
        readerIndex = readerIdx + 8;
        final long pos = BYTE_ARRAY_OFFSET + readerIdx;
        if (LITTLE_ENDIAN)
        {
            return Double.longBitsToDouble(UNSAFE.getLong(array, pos));
        }
        else
        {
            return Double.longBitsToDouble(Long.reverseBytes(UNSAFE.getLong(array, pos)));
        }
    }
//    public void writeBytes(byte[] bytes)
//    {
//        writeBytes(bytes, 0, bytes.length);
//    }
//
//    public void writeBytes(byte[] bytes, int offset, int length)
//    {
//        final int writerIdx = writerIndex;
//        final int newIdx    = writerIdx + length;
//        ensureNewWriterIndex(newIdx);
//        put(writerIdx, bytes, offset, length);
//        writerIndex = newIdx;
//    }
//
//    private void put(int index, byte[] src, int offset, int length)
//    {
//        final long pos        = BYTE_ARRAY_OFFSET + index;
//        long       srcAddress = BYTE_ARRAY_OFFSET + offset;
//        copyMemory(src, srcAddress, array, pos, length);
//    }
//
//    public byte[] readBytes(int length)
//    {
//        int readerIdx = readerIndex;
//        // use subtract to avoid overflow
//        if (readerIdx > writerIndex - length)
//        {
//            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, length, writerIndex, this));
//        }
//        byte[]       heapMemory = array;
//        final byte[] bytes      = new byte[length];
//        System.arraycopy(heapMemory, BYTE_ARRAY_OFFSET + readerIdx, bytes, 0, length);
//        readerIndex = readerIdx + length;
//        return bytes;
//    }

    public void writeBytesWithSizeEmbedded(byte[] bytes)
    {
        writePrimitiveArrayWithSizeEmbedded(bytes, 0, bytes.length);
    }

    public void writePrimitiveArrayWithSizeEmbedded(Object arr, int arrOffset, int numBytes)
    {
        int idx = writerIndex;
        ensureNewWriterIndex(idx + 8 + numBytes);
        idx += writePositiveVarIntWithoutEnsure(numBytes);
        final long destAddr = BYTE_ARRAY_OFFSET + idx;
        copyMemory(arr, BYTE_ARRAY_OFFSET + arrOffset, array, destAddr, numBytes);
        writerIndex = idx + numBytes;
    }

    public byte[] readBytesWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final byte[] arr = new byte[numBytes];
        System.arraycopy(array, readerIdx, arr, 0, numBytes);
        readerIndex = readerIdx + numBytes;
        return arr;
    }

    public char[] readCharsWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final char[] chars = new char[numBytes / 2];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, chars, CHAR_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return chars;
    }

    public void writeCharsWithSizeEmbedded(char[] chars)
    {
        writePrimitiveArrayWithSizeEmbedded(chars, 0, Math.multiplyExact(chars.length, 2));
    }

    public void writeBooleansWithSizeEmbedded(boolean[] booleans)
    {
        writePrimitiveArrayWithSizeEmbedded(booleans, 0, booleans.length);
    }

    public void writeShortsWithSizeEmbedded(short[] shorts)
    {
        writePrimitiveArrayWithSizeEmbedded(shorts, 0, Math.multiplyExact(shorts.length, 2));
    }

    public void writeIntsWithSizeEmbedded(int[] ints)
    {
        writePrimitiveArrayWithSizeEmbedded(ints, 0, Math.multiplyExact(ints.length, 4));
    }

    public void writeLongsWithSizeEmbedded(long[] longs)
    {
        writePrimitiveArrayWithSizeEmbedded(longs, 0, Math.multiplyExact(longs.length, 8));
    }

    public void writeFloatsWithSizeEmbedded(float[] floats)
    {
        writePrimitiveArrayWithSizeEmbedded(floats, 0, Math.multiplyExact(floats.length, 4));
    }

    public void writeDoublesWithSizeEmbedded(double[] doubles)
    {
        writePrimitiveArrayWithSizeEmbedded(doubles, 0, Math.multiplyExact(doubles.length, 8));
    }

    public short[] readShortsWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final short[] shorts = new short[numBytes / 2];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, shorts, SHORT_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return shorts;
    }

    public int[] readIntsWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final int[] ints = new int[numBytes / 4];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, ints, INT_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return ints;
    }

    public long[] readLongsWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final long[] longs = new long[numBytes / 8];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, longs, LONG_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return longs;
    }

    public float[] readFloatsWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final float[] floats = new float[numBytes / 4];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, floats, FLOAT_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return floats;
    }

    public double[] readDoublesWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final double[] doubles = new double[numBytes / 8];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, doubles, DOUBLE_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return doubles;
    }

    public boolean[] readBooleansWithSizeEmbedded()
    {
        final int numBytes  = readPositiveVarInt();
        int       readerIdx = readerIndex;
        // use subtract to avoid overflow
        if (readerIdx > writerIndex - numBytes)
        {
            throw new IndexOutOfBoundsException(String.format("readerIdx(%d) + length(%d) exceeds size(%d): %s", readerIdx, numBytes, writerIndex, this));
        }
        final boolean[] booleans = new boolean[numBytes];
        copyMemory(array, BYTE_ARRAY_OFFSET + readerIdx, booleans, BOOLEAN_ARRAY_OFFSET, numBytes);
        readerIndex = readerIdx + numBytes;
        return booleans;
    }

    public static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length)
    {
        if (length < UNSAFE_COPY_THRESHOLD)
        {
            UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, length);
        }
        else
        {
            while (length > 0)
            {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
                length -= size;
                srcOffset += size;
                dstOffset += size;
            }
        }
    }

    public void writeString(String value)
    {
        byte[] bytes    = (byte[]) UNSAFE.getReference(value, STRING_VALUE_FIELD_OFFSET);
        byte   coder    = UNSAFE.getByte(value, STRING_CODER_FIELD_OFFSET);
        int    idx      = writerIndex;
        int    numBytes = bytes.length;
        ensureNewWriterIndex(idx + 9 + numBytes);
        UNSAFE.putByte(array, BYTE_ARRAY_OFFSET + idx, coder);
        writerIndex = idx + 1;
        idx += writePositiveVarIntWithoutEnsure(numBytes) + 1;
        System.arraycopy(bytes, 0, array, idx, numBytes);
        writerIndex = idx + numBytes;
    }

    public String readString()
    {
        int  idx   = readerIndex;
        byte coder = array[idx++];
        // The encoding algorithm are based on kryo UnsafeMemoryOutput.writeVarInt
        // varint are written using little endian byte order.
        // inline the implementation here since java can't return varIntBytes and varint
        // at the same time.
        int b        = array[idx++];
        int numBytes = b & 0x7F;
        if ((b & 0x80) != 0)
        {
            b = array[idx++];
            numBytes |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0)
            {
                b = array[idx++];
                numBytes |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0)
                {
                    b = array[idx++];
                    numBytes |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0)
                    {
                        b = array[idx];
                        numBytes |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        final byte[] bytes = new byte[numBytes];
        System.arraycopy(array, idx, bytes, 0, numBytes);
        readerIndex = idx + numBytes;
        if (coder == LATIN1)
        {
            return PRIVATE_STRING_CONSTRUCTOR.apply(bytes, LATIN1_BOXED);
        }
        else
        {
            return PRIVATE_STRING_CONSTRUCTOR.apply(bytes, UTF16_BOXED);
        }
    }

    public boolean remainRead()
    {
        return readerIndex < writerIndex;
    }
}
