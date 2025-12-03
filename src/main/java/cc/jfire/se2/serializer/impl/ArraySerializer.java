package cc.jfire.se2.serializer.impl;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.JfireSE;
import cc.jfire.se2.classinfo.ClassInfo;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

import java.lang.reflect.Array;

import static cc.jfire.se2.JfireSE.*;

public class ArraySerializer implements Serializer
{
    private final Class<?>  componentType;
    private       ClassInfo typeDefinedClassInfo;
    private final JfireSE   jfireSE;

    public ArraySerializer(Class<?> clazz, JfireSE jfireSE)
    {
        this.jfireSE       = jfireSE;
        this.componentType = clazz.getComponentType();
    }

    @Override
    public void init()
    {
        typeDefinedClassInfo = jfireSE.getOrCreateClassInfo(componentType);
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        Object[] arr = (Object[]) instance;
        byteArray.writePositiveVarInt(arr.length);
        for (Object t : arr)
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
        int      length = byteArray.readPositiveVarInt();
        Object[] arr    = (Object[]) Array.newInstance(componentType, length);
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
                         ID_CONTENT_UN_TRACK -> arr[i] = jfireSE.readByUnderInstanceIdFlag(byteArray, flag);
                    case JfireSE.INSTANCE_ID -> arr[i] = typeDefinedClassInfo.getInstanceById(byteArray.readPositiveVarInt());
                    case JfireSE.CONTENT_TRACK -> arr[i] = typeDefinedClassInfo.readWithTrack(byteArray);
                    case JfireSE.CONTENT_UN_TRACK -> arr[i] = typeDefinedClassInfo.readWithoutTrack(byteArray);
                    default -> throw new RuntimeException("未知的序列化类型");
                }
            }
        }
        return arr;
    }
}
