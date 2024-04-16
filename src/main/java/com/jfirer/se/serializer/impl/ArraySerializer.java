package com.jfirer.se.serializer.impl;

import com.jfirer.se.ByteArray;
import com.jfirer.se.ClassInfo;
import com.jfirer.se.JfireSE;
import com.jfirer.se.serializer.Serializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

@SuppressWarnings("rawtypes")
public class ArraySerializer implements Serializer
{
    private              Class     componentType;
    private              int       arrayBaseOffset;
    private              int       arrayIndexScale;
    private              int       arrayIndexScaleShift;
    private              boolean   componentTypeFinal;
    private              ClassInfo classInfoOfComponentTypeFinal;
    private              ClassInfo cachedClassInfo;
    private              JfireSE   jfireSE;
    private static final Unsafe    UNSAFE = Unsafe.getUnsafe();

    public ArraySerializer(Class clazz, JfireSE jfireSE)
    {
        this.jfireSE         = jfireSE;
        componentType        = clazz.getComponentType();
        arrayBaseOffset      = UNSAFE.arrayBaseOffset(clazz);
        arrayIndexScale      = UNSAFE.arrayIndexScale(clazz);
        arrayIndexScaleShift = arrayIndexScale == 4 ? 2 : 3;
        componentTypeFinal   = Modifier.isFinal(componentType.getModifiers());
        if (componentTypeFinal)
        {
            classInfoOfComponentTypeFinal = jfireSE.getClassInfo(componentType);
        }
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        int length = ((Object[]) instance).length;
        byteArray.writeVarInt(length);
        for (int i = 0; i < length; i++)
        {
            Object element = UNSAFE.getReference(instance, arrayBaseOffset + ((long) i << arrayIndexScaleShift));
            if (element == null)
            {
                byteArray.put(JfireSE.NULL);
            }
            else
            {
                if (componentTypeFinal)
                {
                    classInfoOfComponentTypeFinal.writeBytes(byteArray, element, true);
                }
                else
                {
                    if (cachedClassInfo != null && cachedClassInfo.getClazz() == element.getClass())
                    {
                        cachedClassInfo.writeBytes(byteArray, element, false);
                    }
                    else
                    {
                        cachedClassInfo = jfireSE.getClassInfo(element.getClass());
                        cachedClassInfo.writeBytes(byteArray, element, false);
                    }
                }
            }
        }
    }

    @Override
    public Object readBytes(ByteArray byteArray)
    {
        int    length = byteArray.readVarInt();
        Object array  = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++)
        {
            if (componentTypeFinal)
            {
                int flag = byteArray.readVarInt();
                switch (flag)
                {
                    case JfireSE.NULL -> UNSAFE.putReference(array, arrayBaseOffset + ((long) i << arrayIndexScaleShift), null);
                    case 7 -> UNSAFE.putReference(array, arrayBaseOffset + ((long) i << arrayIndexScaleShift), classInfoOfComponentTypeFinal.readBytes(byteArray, true));
                    case 8 -> UNSAFE.putReference(array, arrayBaseOffset + ((long) i << arrayIndexScaleShift), classInfoOfComponentTypeFinal.readBytes(byteArray, false));
                    case 9 -> UNSAFE.putReference(array, arrayBaseOffset + ((long) i << arrayIndexScaleShift), classInfoOfComponentTypeFinal.getTracking(byteArray.readVarInt()));
                    default -> throw new IllegalArgumentException();
                }
            }
            else
            {
                UNSAFE.putReference(array, arrayBaseOffset + ((long) i << arrayIndexScaleShift), jfireSE.readBytes(byteArray));
            }
        }
        return array;
    }
}
