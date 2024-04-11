package com.jfirer.se;

import com.jfirer.se.serializer.SerializerResolver;

import java.util.IdentityHashMap;
import java.util.Map;

public class ClassInfoResolver
{
    public static int                   NO_CLASS_ID    = 0;
    private       Map<Class, ClassInfo> store          = new IdentityHashMap<>();
    private       int                   currentClassId = 1;
    private       int                   fixedClassId   = 1;
    private       ClassInfo[]           tracking       = new ClassInfo[32];
    private       JfireSE               jfireSE;
    private       SerializerResolver    resolver;

    public ClassInfoResolver(SerializerResolver resolver, JfireSE jfireSE)
    {
        this.resolver = resolver;
        this.jfireSE  = jfireSE;
    }

    public void getClassId(ClassInfo classInfo)
    {
        if (currentClassId > tracking.length)
        {
            ClassInfo[] newTracking = new ClassInfo[tracking.length << 1];
            System.arraycopy(tracking, 0, newTracking, 0, tracking.length);
            tracking = newTracking;
        }
        tracking[currentClassId] = classInfo;
        classInfo.setClassId(currentClassId);
        currentClassId += 1;
    }

    public void reset()
    {
        for (int i = currentClassId - 1; i > fixedClassId; i--)
        {
            tracking[i].reset();
        }
        currentClassId = fixedClassId;
    }

    public ClassInfo getClassInfo(Class clazz)
    {
        ClassInfo classInfo = store.get(clazz);
        if (classInfo == null)
        {
            classInfo = new ClassInfo().setClassName(clazz.getName()).setJfireSE(jfireSE).setClazz(clazz);
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
            classInfo = new ClassInfo().setClassName(clazz.getName()).setJfireSE(jfireSE).setClazz(clazz);
            store.put(clazz, classInfo);
        }
        for (int i = 1; i < fixedClassId; i++)
        {
            if (tracking[i].getClazz() == clazz)
            {
                return;
            }
        }
        if (fixedClassId > tracking.length)
        {
            ClassInfo[] newTracking = new ClassInfo[tracking.length << 1];
            System.arraycopy(tracking, 0, newTracking, 0, tracking.length);
            tracking = newTracking;
        }
        tracking[fixedClassId] = classInfo;
        classInfo.setClassId(fixedClassId++);
        currentClassId = fixedClassId;
    }
}
