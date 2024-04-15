package com.jfirer.se.serializer;

import com.jfirer.fse.serializer.array.*;
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

}
