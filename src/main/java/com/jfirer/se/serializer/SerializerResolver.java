package com.jfirer.se.serializer;

import com.jfirer.se.JfireSE;

import java.util.IdentityHashMap;
import java.util.Map;

public class SerializerResolver
{
    private Map<Class, Serializer> store = new IdentityHashMap<>();

    public Serializer getSerializer(Class clazz, JfireSE jfireSE)
    {
        Serializer serializer = store.get(clazz);
        if (serializer != null)
        {
            return serializer;
        }

    }
    public void registerSerializer(Class clazz, Serializer serializer)
    {
        store.put(clazz, serializer);
    }
}
