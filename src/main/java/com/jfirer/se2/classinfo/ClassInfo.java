package com.jfirer.se2.classinfo;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.serializer.Serializer;
import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Data
public abstract class ClassInfo implements RefTracking
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

    @Override
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

    /**
     * 进行完整的对象实例化，并且根据初始配置情况自动决定是否放入追踪。
     *
     * @param byteArray
     * @param instance
     */
    public abstract void write(ByteArray byteArray, Object instance);

    /**
     * 进行完整的对象实例化，但是是在已知对象类型的情况。输出的标志位只在 3 个中可选。
     *
     * @param byteArray
     * @param instance
     */
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
     * 读取对象的内容，并且这个对象本身要放入追踪。
     * 这个读取意味着标志位和标志信息已经被读取完毕，此时只是单纯读取对象实例的序列化内容。
     *
     * @param byteArray
     * @return
     */
    public Object readWithTrack(ByteArray byteArray)
    {
        return serializer.read(byteArray, this);
    }

    /**
     * 读取对象的内容，但是这个对象本身不放入追踪。
     * 这个读取意味着标志位和标志信息已经被读取完毕，此时只是单纯读取对象实例的序列化内容。
     *
     * @param byteArray
     * @return
     */
    public Object readWithoutTrack(ByteArray byteArray)
    {
        return serializer.read(byteArray, null);
    }

    @Override
    public Object getInstanceById(int instanceId)
    {
        return tracking[instanceId];
    }
}
