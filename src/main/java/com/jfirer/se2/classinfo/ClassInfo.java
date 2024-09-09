package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.serializer.Serializer;
import lombok.Data;

import java.util.Arrays;

@Data
public abstract class ClassInfo
{
    protected final short      classId;
    protected final String     className;
    protected final Class<?>   clazz;
    protected final boolean    refTrack;
    protected       Object[]   tracking;
    protected       int        refTrackingIndex = 0;
    protected       Serializer serializer;

    public int addTracking(Object instance)
    {
        if (tracking == null)
        {
            tracking = new Object[4];
        }
        for (int i = 0; i < refTrackingIndex; i++)
        {
            if (tracking[i] == instance)
            {
                return i;
            }
        }
        if (refTrackingIndex == tracking.length)
        {
            Object[] newRefTracking = new Object[tracking.length * 2];
            System.arraycopy(tracking, 0, newRefTracking, 0, tracking.length);
            tracking = newRefTracking;
        }
        tracking[refTrackingIndex++] = instance;
        return -1;
    }

    public void reset()
    {
        if (refTrackingIndex != 0)
        {
            Arrays.fill(tracking, 0, refTrackingIndex, null);
            refTrackingIndex = 0;
        }
    }

    public abstract void write(ByteArray byteArray, Object instance);

    public void writeKnownClazz(ByteArray byteArray, Object instance)
    {
        if (refTrack)
        {
            int index = addTracking(instance);
            if (index == -1)
            {
                byteArray.put(JfireSE.content_track);
                serializer.writeBytes(byteArray, instance);
            }
            else
            {
                byteArray.put(JfireSE.instance_id);
                byteArray.writeVarInt(index);
            }
        }
        else
        {
            byteArray.put(JfireSE.content_un_track);
            serializer.writeBytes(byteArray, instance);
        }
    }

    public abstract void readWithTrack(ByteArray byteArray);
}
