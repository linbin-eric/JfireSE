package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.classinfo.ClassInfo;

import java.lang.reflect.Field;

public class FinalFieldInfo extends FieldInfo
{
    private ClassInfo   classInfo;
    private JfireSEImpl jfireSE;

    public FinalFieldInfo(Field field, JfireSEImpl jfireSE)
    {
        super(field);
        classInfo    = jfireSE.getForSerialize(field.getType());
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
                case JfireSE.INSTANCE_ID ->
                {
                    int    instanceId = byteArray.readVarInt();
                    Object property   = classInfo.getInstanceById(instanceId);
                    accessor.setObject(instance, property);
                }
                case JfireSE.CONTENT_TRACK ->
                {
                    Object property = classInfo.readWithTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.CONTENT_UN_TRACK ->
                {
                    Object property = classInfo.readWithoutTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                default -> throw new RuntimeException("flag:" + flag);
            }
        }
    }
}
