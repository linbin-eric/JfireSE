package com.jfirer.se;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

public class ClassInfoResolver
{
    public static int                   NO_CLASS_ID      = 0;
    private       Map<Class, ClassInfo> store            = new IdentityHashMap<>();
    private       int                   nextTempClassId  = 1;
    private       int                   tempClassIdStart = 1;
    private       ClassInfo[]           tracking         = new ClassInfo[32];
    private       JfireSE               jfireSE;

    public ClassInfoResolver(JfireSE jfireSE)
    {
        this.jfireSE = jfireSE;
        registerClass(int[].class);
        registerClass(long[].class);
        registerClass(short[].class);
        registerClass(float[].class);
        registerClass(double[].class);
        registerClass(char[].class);
        registerClass(boolean[].class);
        registerClass(byte[].class);
        registerClass(String[].class);
        registerClass(Integer[].class);
        registerClass(Long[].class);
        registerClass(Short[].class);
        registerClass(Float[].class);
        registerClass(Double[].class);
        registerClass(Character[].class);
        registerClass(Boolean[].class);
        registerClass(ByteArray[].class);
        registerClass(String.class);
        registerClass(Integer.class);
        registerClass(Long.class);
        registerClass(Short.class);
        registerClass(Float.class);
        registerClass(Double.class);
        registerClass(Character.class);
        registerClass(Boolean.class);
        registerClass(ByteArray.class);
        registerClass(Date.class);
        registerClass(java.sql.Date.class);
        registerClass(LocalDateTime.class);
    }

    public void getClassId(ClassInfo classInfo)
    {
        if (nextTempClassId > tracking.length)
        {
            ClassInfo[] newTracking = new ClassInfo[tracking.length << 1];
            System.arraycopy(tracking, 0, newTracking, 0, tracking.length);
            tracking = newTracking;
        }
        tracking[nextTempClassId] = classInfo;
        classInfo.setClassId(nextTempClassId);
        nextTempClassId += 1;
    }

    public void reset()
    {
        for (int i = 1; i < nextTempClassId; i++)
        {
            tracking[i].reset(tempClassIdStart);
        }
        nextTempClassId = tempClassIdStart;
    }

    public ClassInfo getClassInfo(Class clazz)
    {
        ClassInfo classInfo = store.get(clazz);
        if (classInfo == null)
        {
            classInfo = new ClassInfo().setClassName(clazz.getName()).setJfireSE(jfireSE).setRefTracking(jfireSE.isRefTracking()).setClazz(clazz);
            store.put(clazz, classInfo);
            return classInfo;
        }
        return classInfo;
    }

    public void registerClass(Class clazz)
    {
        ClassInfo classInfo = getClassInfo(clazz);
        if (classInfo == null)
        {
            classInfo = new ClassInfo().setClassName(clazz.getName()).setJfireSE(jfireSE).setRefTracking(jfireSE.isRefTracking()).setClazz(clazz);
            store.put(clazz, classInfo);
        }
        for (int i = 1; i < tempClassIdStart; i++)
        {
            if (tracking[i].getClazz() == clazz)
            {
                return;
            }
        }
        if (tempClassIdStart > tracking.length)
        {
            ClassInfo[] newTracking = new ClassInfo[tracking.length << 1];
            System.arraycopy(tracking, 0, newTracking, 0, tracking.length);
            tracking = newTracking;
        }
        tracking[tempClassIdStart] = classInfo;
        classInfo.setClassId(tempClassIdStart);
        tempClassIdStart += 1;
        nextTempClassId = tempClassIdStart;
    }

    public ClassInfo getClassInfo(int classId)
    {
        return tracking[classId];
    }
}
