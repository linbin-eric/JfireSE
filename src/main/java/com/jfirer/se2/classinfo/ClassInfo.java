package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.serializer.Serializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Data
public abstract class ClassInfo
{
    protected final        short      classId;
    protected final        byte[]     classNameBytes;
    protected final        Class<?>   clazz;
    protected final        boolean    refTrack;
    protected              Object[]   tracking;
    protected              int        refTrackingIndex = 0;
    protected              Serializer serializer;
    protected static final Unsafe     UNSAFE           = Unsafe.getUnsafe();

    public ClassInfo(short classId, Class<?> clazz, boolean refTrack)
    {
        this.classId   = classId;
        this.clazz     = clazz;
        this.refTrack  = refTrack;
        classNameBytes = clazz.getName().getBytes(StandardCharsets.UTF_8);
    }

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
                byteArray.put(JfireSE.CONTENT_TRACK);
                serializer.writeBytes(byteArray, instance);
            }
            else
            {
                byteArray.put(JfireSE.INSTANCE_ID);
                byteArray.writePositiveVarInt(index);
            }
        }
        else
        {
            byteArray.put(JfireSE.CONTENT_UN_TRACK);
            serializer.writeBytes(byteArray, instance);
        }
    }

    /**
     * 读取对象的内容，并且这个对象本身要放入追踪
     *
     * @param byteArray
     * @return
     */
    public Object readWithTrack(ByteArray byteArray)
    {
        try
        {
            Object instance = UNSAFE.allocateInstance(clazz);
            addTracking(instance);
            serializer.read(byteArray, instance);
            return instance;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object readWithoutTrack(ByteArray byteArray)
    {
        try
        {
            Object instance = UNSAFE.allocateInstance(clazz);
            serializer.read(byteArray, instance);
            return instance;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object getInstanceById(int instanceId)
    {
        return tracking[instanceId];
    }
}
