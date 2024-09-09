package com.jfirer.se2.serializer;

import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.serializer.impl.ObjectSerializer.ObjectSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory
{
    private Map<Class<?>, Serializer> store = new HashMap<>();

    public Serializer getSerializer(Class<?> clazz, JfireSEImpl jfireSE)
    {
        return store.putIfAbsent(clazz, new ObjectSerializer(clazz, jfireSE));
    }
}
