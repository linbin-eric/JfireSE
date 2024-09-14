package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;

/**
 * 事先注册的类，这样在序列化的时候只需要序列化 classId 即可。
 */
public class RegisterClasInfo extends ClassInfo
{
    public RegisterClasInfo(short classId, Class<?> clazz, boolean refTracking)
    {
        super(classId, clazz, refTracking);
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        if (refTrack)
        {
            int i = addTracking(instance);
            if (i == -1)
            {
                byteArray.put(JfireSE.ID_CONTENT_TRACK);
                byteArray.writePositiveVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
            else
            {
                byteArray.put(JfireSE.ID_INSTANCE_ID);
                byteArray.writePositiveVarInt(classId);
                byteArray.writePositiveVarInt(i);
            }
            if (!needClean)
            {
                needClean = true;
                jfireSE.scheduleForClean(this);
            }
        }
        else
        {
            byteArray.put(JfireSE.ID_CONTENT_UN_TRACK);
            byteArray.writePositiveVarInt(classId);
            serializer.writeBytes(byteArray, instance);
        }
    }
}
