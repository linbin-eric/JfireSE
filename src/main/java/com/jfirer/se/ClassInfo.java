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
    private Object[]   refTracking;
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
            if (jfireSE.isCycleSupport())
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
            if (jfireSE.isCycleSupport())
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

    public void reset()
    {
        classId          = ClassInfoResolver.NO_CLASS_ID;
        refTrackingIndex = 0;
    }

    public int addTracking(Object instance)
    {
        if (refTracking == null)
        {
            refTracking = new Object[4];
        }
        for (int i = 0; i < refTrackingIndex; i++)
        {
            if (refTracking[i] == instance)
            {
                return i;
            }
        }
        if (refTrackingIndex == refTracking.length)
        {
            Object[] newRefTracking = new Object[refTracking.length * 2];
            System.arraycopy(refTracking, 0, newRefTracking, 0, refTracking.length);
            refTracking = newRefTracking;
        }
        refTracking[refTrackingIndex++] = instance;
        return -1;
    }
}
