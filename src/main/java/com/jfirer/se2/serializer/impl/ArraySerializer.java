package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.ClassInfo;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

import java.lang.reflect.Array;

import static com.jfirer.se2.JfireSE.*;

public class ArraySerializer<T> implements Serializer
{
    private       Class<?>  componentType;
    private final ClassInfo typeDefinedClassInfo;
    private       JfireSE   jfireSE;

    public ArraySerializer(Class<T[]> clazz, JfireSE jfireSE)
    {
        this.jfireSE         = jfireSE;
        this.componentType   = clazz.getComponentType();
        typeDefinedClassInfo = jfireSE.getOrCreateClassInfo(componentType);
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
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        int length = byteArray.readPositiveVarInt();
        T[] arr    = (T[]) Array.newInstance(componentType, length);
        if (refTracking != null)
        {
            refTracking.addTracking(arr);
        }
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
                    case NAME_ID_CONTENT_TRACK, NAME_ID_CONTENT_UN_TRACK, ID_INSTANCE_ID, ID_CONTENT_TRACK,
                         ID_CONTENT_UN_TRACK -> arr[i] = (T) jfireSE.readByUnderInstanceIdFlag(byteArray, flag);
                    case JfireSE.INSTANCE_ID -> arr[i] = (T) typeDefinedClassInfo.getInstanceById(byteArray.readPositiveVarInt());
                    case JfireSE.CONTENT_TRACK -> arr[i] = (T) typeDefinedClassInfo.readWithTrack(byteArray);
                    case JfireSE.CONTENT_UN_TRACK -> arr[i] = (T) typeDefinedClassInfo.readWithoutTrack(byteArray);
                    default -> throw new RuntimeException("未知的序列化类型");
                }
            }
        }
        return arr;
    }
}
