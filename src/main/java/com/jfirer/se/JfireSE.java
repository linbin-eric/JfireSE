package com.jfirer.se;

import com.jfirer.se.serializer.Serializer;
import com.jfirer.se.serializer.SerializerResolver;

public class JfireSE
{
    public static final byte               NULL          = 0;
    private             boolean            CYCLE_SUPPORT = true;
    private             SerializerResolver serializerResolver;
    private             ClassInfoResolver  classInfoResolver;
    private             int                depth         = 1;

    public boolean isCycleSupport()
    {
        return CYCLE_SUPPORT;
    }

    public Serializer getSerializer(Class clazz)
    {
        return serializerResolver.getSerializer(clazz, this);
    }

    public ClassInfo getClassInfo(Class clazz)
    {
        return classInfoResolver.getClassInfo(clazz);
    }

    public void getClassId(ClassInfo classInfo)
    {
        classInfoResolver.getClassId(classInfo);
    }

    public void incrDepth()
    {
        if (depth++ > 256)
        {
            throw new IllegalStateException("序列化深度超过256，可能存在循环引用，请开启循环引用设置");
        }
    }

    public void reduceDepth()
    {
        depth--;
    }
}
