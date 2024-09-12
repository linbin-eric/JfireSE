package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.ClassInfo;

import java.lang.reflect.Field;

public class VariableFieldInfo extends FieldInfo
{
    private final JfireSE   jfireSE;
    private       ClassInfo classInfo;
    private final ClassInfo firstClassInfo;

    public VariableFieldInfo(Field field, JfireSE jfireSE)
    {
        super(field);
        classInfo    = jfireSE.getOrCreateClassInfo(field.getType());
        this.jfireSE = jfireSE;
        if (field.getType().isInterface())
        {
            firstClassInfo = null;
        }
        else
        {
            firstClassInfo = classInfo;
        }
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        Object obj = accessor.get(instance);
        if (obj == null)
        {
            byteArray.put(JfireSE.NULL);
        }
        else
        {
            Class<?> objClass = obj.getClass();
            if (objClass == classInfo.getClazz())
            {
                if (classInfo == firstClassInfo)
                {
                    classInfo.writeKnownClazz(byteArray, obj);
                }
                else
                {
                    classInfo.write(byteArray, obj);
                }
            }
            else
            {
                classInfo = jfireSE.getOrCreateClassInfo(objClass);
                if (classInfo == firstClassInfo)
                {
                    classInfo.writeKnownClazz(byteArray, obj);
                }
                else
                {
                    classInfo.write(byteArray, obj);
                }
            }
        }
    }

    @Override
    public void read(ByteArray byteArray, Object instance)
    {
        byte flag = byteArray.get();
        if (flag == JfireSE.NULL)
        {
            accessor.setObject(instance, null);
        }
        else
        {
            switch (flag)
            {
                case JfireSE.NAME_ID_CONTENT_TRACK,JfireSE.NAME_ID_CONTENT_UN_TRACK, JfireSE.ID_INSTANCE_ID,JfireSE.ID_CONTENT_TRACK,JfireSE.ID_CONTENT_UN_TRACK -> accessor.setObject(instance, jfireSE.readByUnderInstanceIdFlag(byteArray, flag));
                case JfireSE.INSTANCE_ID -> accessor.setObject(instance, firstClassInfo.getInstanceById(byteArray.readPositiveVarInt()));
                case JfireSE.CONTENT_TRACK -> accessor.setObject(instance, firstClassInfo.readWithTrack(byteArray));
                case JfireSE.CONTENT_UN_TRACK -> accessor.setObject(instance, firstClassInfo.readWithoutTrack(byteArray));
                default -> throw new RuntimeException("flag:" + flag);
            }
        }
    }
}
