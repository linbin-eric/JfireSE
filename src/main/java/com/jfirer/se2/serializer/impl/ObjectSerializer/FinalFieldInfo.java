package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.ClassInfo;

import java.lang.reflect.Field;

public class FinalFieldInfo extends FieldInfo
{
    private ClassInfo   classInfo;
    private JfireSE jfireSE;

    public FinalFieldInfo(Field field, JfireSE jfireSE)
    {
        super(field);
        classInfo    = jfireSE.getOrCreateClassInfo(field.getType());
        this.jfireSE = jfireSE;
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
            classInfo.writeKnownClazz(byteArray, obj);
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
                case JfireSE.INSTANCE_ID -> accessor.setObject(instance, classInfo.getInstanceById(byteArray.readPositiveVarInt()));
                case JfireSE.CONTENT_TRACK -> accessor.setObject(instance, classInfo.readWithTrack(byteArray));
                case JfireSE.CONTENT_UN_TRACK -> accessor.setObject(instance, classInfo.readWithoutTrack(byteArray));
                default -> throw new RuntimeException("flag:" + flag);
            }
        }
    }
}
