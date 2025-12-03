package cc.jfire.se2.serializer.impl;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

public abstract class PrimitiveArraySerializer<T> implements Serializer
{
    public static class IntArraySerializer extends PrimitiveArraySerializer<int[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            int[] arr = (int[]) instance;
            byteArray.writeIntsWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            int[] ints = byteArray.readIntsWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(ints);
            }
            return ints;
        }
    }

    public static class LongArraySerializer extends PrimitiveArraySerializer<long[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            long[] arr = (long[]) instance;
            byteArray.writeLongsWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            long[] longs = byteArray.readLongsWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(longs);
            }
            return longs;
        }
    }

    public static class FloatArraySerializer extends PrimitiveArraySerializer<float[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            float[] arr = (float[]) instance;
            byteArray.writeFloatsWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            float[] floats = byteArray.readFloatsWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(floats);
            }
            return floats;
        }
    }

    public static class DoubleArraySerializer extends PrimitiveArraySerializer<double[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            double[] arr = (double[]) instance;
            byteArray.writeDoublesWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            double[] doubles = byteArray.readDoublesWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(doubles);
            }
            return doubles;
        }
    }

    public static class BooleanArraySerializer extends PrimitiveArraySerializer<boolean[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            boolean[] arr = (boolean[]) instance;
            byteArray.writeBooleansWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            boolean[] booleans = byteArray.readBooleansWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(booleans);
            }
            return booleans;
        }
    }

    public static class ByteArraySerializer extends PrimitiveArraySerializer<byte[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            byte[] arr = (byte[]) instance;
            byteArray.writeBytesWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            byte[] bytes = byteArray.readBytesWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(bytes);
            }
            return bytes;
        }
    }

    public static class CharArraySerializer extends PrimitiveArraySerializer<char[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            char[] arr = (char[]) instance;
            byteArray.writeCharsWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            char[] chars = byteArray.readCharsWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(chars);
            }
            return chars;
        }
    }
    public static  class ShortArraySerializer extends PrimitiveArraySerializer<short[]>
    {
        @Override
        public void writeBytes(ByteArray byteArray, Object instance)
        {
            short[] arr = (short[]) instance;
            byteArray.writeShortsWithSizeEmbedded(arr);
        }

        @Override
        public Object read(ByteArray byteArray, RefTracking refTracking)
        {
            short[] shorts = byteArray.readShortsWithSizeEmbedded();
            if (refTracking != null)
            {
                refTracking.addTracking(shorts);
            }
            return shorts;
        }
    }
}
