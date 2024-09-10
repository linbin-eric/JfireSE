package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;

public class StaticClasInfo extends ClassInfo
{
    public StaticClasInfo(short classId, Class<?> clazz, boolean refTracking)
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
                byteArray.writeVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
            else
            {
                byteArray.put(JfireSE.ID_INSTANCE_ID);
                byteArray.writeVarInt(classId);
                byteArray.writeVarInt(i);
            }
        }
        else
        {
            byteArray.put(JfireSE.ID_CONTENT_UN_TRACK);
            byteArray.writeVarInt(classId);
            serializer.writeBytes(byteArray, instance);
        }
    }

    @Override
    public Object readWithTrack(ByteArray byteArray)
    {
        return null;
    }
}
