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
    private boolean firstSerialized = true;

    public DynamicClassInfo(short classId, Class<?> clazz, boolean refTracking)
    {
        super(classId, clazz, refTracking);
    }

    @Override
    public void reset()
    {
        super.reset();
        firstSerialized = true;
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        if (refTrack)
        {
            if (firstSerialized)
            {
                firstSerialized = true;
                addTracking(instance);
                byteArray.put(JfireSE.NAME_ID_CONTENT_TRACK);
                byteArray.writeBytesWithSizeEmbedded(classNameBytes);
                byteArray.writeVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
            else
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
        }
        else
        {
            if (firstSerialized)
            {
                firstSerialized = true;
                byteArray.put(JfireSE.NAME_ID_CONTENT_UN_TRACK);
                byteArray.writeBytesWithSizeEmbedded(classNameBytes);
                byteArray.writeVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
            else
            {
                byteArray.put(JfireSE.ID_CONTENT_UN_TRACK);
                byteArray.writeVarInt(classId);
                serializer.writeBytes(byteArray, instance);
            }
        }
    }
}
