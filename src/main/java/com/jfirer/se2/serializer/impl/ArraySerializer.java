package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.classinfo.ClassInfo;
import com.jfirer.se2.serializer.Serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

public class ArraySerializer<T> implements Serializer
{
    private       Class<?>    componentType;
    private final boolean     isFinal;
    private final ClassInfo   typeDefinedClassInfo;
    private       JfireSEImpl jfireSE;

    public ArraySerializer(Class<T[]> clazz, JfireSEImpl jfireSE)
    {
        this.jfireSE         = jfireSE;
        this.componentType   = clazz.getComponentType();
        typeDefinedClassInfo = jfireSE.getOrCreateClassInfo(componentType);
        isFinal              = Modifier.isFinal(componentType.getModifiers());
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        T[] arr = (T[]) instance;
        byteArray.writePositiveVarInt(arr.length);
        for (T t : arr)
        {
            if (t == null)
            {
                byteArray.put(JfireSE.NULL);
            }
            else
            {
                Class<?> tClass = t.getClass();
                if (tClass == typeDefinedClassInfo.getClazz())
                {
                    typeDefinedClassInfo.writeKnownClazz(byteArray, t);
                }
                else
                {
                    ClassInfo classInfo = jfireSE.getOrCreateClassInfo(tClass);
                    classInfo.write(byteArray, t);
                }
            }
        }
    }

    @Override
    public void read(ByteArray byteArray, Object instance)
    {
        int len = byteArray.readPositiveVarInt();
        T[] arr = (T[]) Array.newInstance(componentType, len);
        for (int i = 0; i < arr.length; i++)
        {
            byte flag = byteArray.get();
            if (flag == JfireSE.NULL)
            {
                arr[i] = null;
            }
            else
            {
                switch (flag)
                {
                    case JfireSE.NAME_ID_CONTENT_TRACK ->
                    {
                        byte[] bytes = byteArray.readBytesWithSizeEmbedded();
                        int    i1    = byteArray.readVarInt();
                    }
                }
            }
        }
    }

    public static void main(String[] args)
    {
        ArraySerializer<Integer> arraySerializer = new ArraySerializer(Integer[].class, (JfireSEImpl) JfireSE.build());
    }
}
