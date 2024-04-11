package com.jfirer.se;

import com.jfirer.se.serializer.Serializer;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClassInfo
{
    private int        classId          = ClassInfoResolver.NO_CLASS_ID;
    private Class      clazz;
    private String     className;
    private Serializer serializer;
    private JfireSE    jfireSE;
    private Object[]   tracking;
    private boolean    refTracking;
    private int        refTrackingIndex = 0;

    public void writeBytes(InternalByteArray byteArray, Object instance, boolean knownClazz)
    {
        if (instance == null)
        {
            byteArray.put(JfireSE.NULL);
            return;
        }
        if (serializer == null)
        {
            serializer = jfireSE.getSerializer(clazz);
        }
        if (knownClazz)
        {
            if (refTracking)
            {
                int tracking = addTracking(instance);
                if (tracking == -1)
                {
                    byteArray.put((byte) 7);
                    serializer.writeBytes(byteArray, instance);
                }
                else
                {
                    byteArray.put((byte) 9);
                    byteArray.writeVarInt(tracking);
                }
            }
            else
            {
                jfireSE.incrDepth();
                byteArray.put((byte) 8);
                serializer.writeBytes(byteArray, instance);
                jfireSE.reduceDepth();
            }
        }
        else
        {
            if (refTracking)
            {
                if (classId == ClassInfoResolver.NO_CLASS_ID)
                {
                    byteArray.put((byte) 1);
                    byteArray.writeString(className);
                    jfireSE.getClassId(this);
                    addTracking(instance);
                    serializer.writeBytes(byteArray, instance);
                }
                else
                {
                    int tracking = addTracking(instance);
                    if (tracking == -1)
                    {
                        byteArray.put((byte) 4);
                        byteArray.writeVarInt(classId);
                        serializer.writeBytes(byteArray, instance);
                    }
                    else
                    {
                        byteArray.put((byte) 6);
                        byteArray.writeVarInt(classId);
                        byteArray.writeVarInt(tracking);
                    }
                }
            }
            else
            {
                jfireSE.incrDepth();
                if (classId == ClassInfoResolver.NO_CLASS_ID)
                {
                    byteArray.put((byte) 2);
                    byteArray.writeString(className);
                    jfireSE.getClassId(this);
                    serializer.writeBytes(byteArray, instance);
                }
                else
                {
                    byteArray.put((byte) 5);
                    byteArray.writeVarInt(classId);
                    serializer.writeBytes(byteArray, instance);
                }
                jfireSE.reduceDepth();
            }
        }
    }

    public void reset(int tempClassIdStart)
    {
        if (classId < tempClassIdStart)
        {
            ;
        }
        else
        {
            classId = ClassInfoResolver.NO_CLASS_ID;
        }
        refTrackingIndex = 0;
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

    /**
     * 解析的是序列化协议中序号 3 的内容
     *
     * @param byteArray
     * @param refTracking
     * @return
     */
    public Object readBytes(InternalByteArray byteArray, boolean refTracking)
    {
        if (serializer == null)
        {
            serializer = jfireSE.getSerializer(clazz);
        }
        Object instance = serializer.readBytes(byteArray);
        if (refTracking)
        {
            addTracking(instance);
        }
        return instance;
    }

    public Object getTracking(int instanceId)
    {
        return tracking[instanceId];
    }
}
