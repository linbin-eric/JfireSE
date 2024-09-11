package com.jfirer.se2.serializer;

import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.serializer.impl.ArraySerializer;
import com.jfirer.se2.serializer.impl.BoxedArraySerializer;
import com.jfirer.se2.serializer.impl.ObjectSerializer.ObjectSerializer;
import com.jfirer.se2.serializer.impl.PrimitiveArraySerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerFactory
{
    private static Map<Class<?>, Serializer> store = new ConcurrentHashMap<>();

    static
    {
        store.put(int[].class, new PrimitiveArraySerializer.IntArraySerializer());
        store.put(long[].class, new PrimitiveArraySerializer.LongArraySerializer());
        store.put(byte[].class, new PrimitiveArraySerializer.ByteArraySerializer());
        store.put(boolean[].class, new PrimitiveArraySerializer.BooleanArraySerializer());
        store.put(char[].class, new PrimitiveArraySerializer.CharArraySerializer());
        store.put(short[].class, new PrimitiveArraySerializer.ShortArraySerializer());
        store.put(double[].class, new PrimitiveArraySerializer.DoubleArraySerializer());
        store.put(float[].class, new PrimitiveArraySerializer.FloatArraySerializer());
        store.put(Integer[].class, new BoxedArraySerializer.IntegerArraySerializer());
        store.put(Long[].class, new BoxedArraySerializer.LongArraySerializer());
        store.put(Byte[].class, new BoxedArraySerializer.ByteArraySerializer());
        store.put(Boolean[].class, new BoxedArraySerializer.BooleanArraySerializer());
        store.put(Character[].class, new BoxedArraySerializer.CharArraySerializer());
        store.put(Short[].class, new BoxedArraySerializer.ShortArraySerializer());
        store.put(Double[].class, new BoxedArraySerializer.DoubleArraySerializer());
        store.put(Float[].class, new BoxedArraySerializer.FloatArraySerializer());
        store.put(String[].class, new BoxedArraySerializer.StringArraySerializer());
    }

    public static Serializer getSerializer(Class<?> clazz, JfireSEImpl jfireSE)
    {
        if (clazz.isArray())
        {
            return store.computeIfAbsent(clazz, key -> new ArraySerializer(key, jfireSE));
        }
        else
        {
            return store.computeIfAbsent(clazz, key -> ObjectSerializer.buildCompileVersion(key, jfireSE));
        }
    }
}
