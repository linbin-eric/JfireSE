package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class DynamicClassInfo extends ClassInfo
{
    /**
     * 首次输出的情况下需要输出类名
     */
    public DynamicClassInfo(short classId, Class<?> clazz, boolean refTracking)
    {
        super(classId, clazz, refTracking);
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        //如果needClean 为 false，意味着这个 classInfo 是首次输出。后续必然需要清理。
        if (!needClean)
        {
            if (refTrack)
            {
                addTracking(instance);
            }
            byteArray.put(JfireSE.NAME_ID_CONTENT_TRACK);
            byteArray.writeString(classNameStringBytes, classNameStringCoder);
            byteArray.writePositiveVarInt(classId);
            serializer.writeBytes(byteArray, instance);
            needClean = true;
            jfireSE.scheduleForClean(this);
        }
        else
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
            }
            else
            {
                byteArray.put(JfireSE.ID_CONTENT_UN_TRACK);
                byteArray.writePositiveVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
        }
    }
}
