package com.jfirer.se;

import com.jfirer.se.serializer.Serializer;
import com.jfirer.se.serializer.SerializerResolver;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
public class JfireSE
{
    public static final byte                  NULL               = 0;
    public static final byte                  NOT_NULL           = 1;
    @Getter
    private             boolean               refTracking        = true;
    private             SerializerResolver    serializerResolver = new SerializerResolver();
    private             ClassInfoResolver     classInfoResolver  = new ClassInfoResolver(this);
    private             int                   depth              = 1;
    private             Map<String, Class<?>> classNameMap       = new HashMap<>();
    private             ByteArray             byteArray          = new ByteArray(1024);

    public Serializer getSerializer(Class clazz)
    {
        return serializerResolver.getSerializer(clazz, this);
    }

    public ClassInfo getClassInfo(Class clazz)
    {
        return classInfoResolver.getClassInfo(clazz);
    }

    public ClassInfo getClassInfo(String className)
    {
        Class<?> clazz = classNameMap.computeIfAbsent(className, name -> {
            try
            {
                return Class.forName(name);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        });
        ClassInfo classInfo = getClassInfo(clazz);
        getClassId(classInfo);
        return classInfo;
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

    public byte[] writeBytes(Object instance)
    {
        if (instance == null)
        {
            byteArray.put(NULL);
        }
        else
        {
            ClassInfo classInfo = getClassInfo(instance.getClass());
            classInfo.writeBytes(byteArray, instance, false);
            classInfoResolver.reset();
        }
        byte[] result = byteArray.toArray();
        byteArray.setWriterIndex(0);
        return result;
    }

    public Object readBytes(byte[] bytes)
    {
        return readBytes(new ByteArray(bytes));
    }

    public Object readBytes(ByteArray byteArray)
    {
        byte flag = byteArray.get();
        switch (flag)
        {
            case 0 -> {return null;}
            case 1 ->
            {
                String    className = byteArray.readString();
                ClassInfo classInfo = getClassInfo(className);
                return classInfo.readBytes(byteArray, true);
            }
            case 2 ->
            {
                String    className = byteArray.readString();
                ClassInfo classInfo = getClassInfo(className);
                return classInfo.readBytes(byteArray, false);
            }
            case 3, 7, 8, 9 ->
            {
                throw new IllegalArgumentException();
            }
            case 4 ->
            {
                int       classId   = byteArray.readVarInt();
                ClassInfo classInfo = classInfoResolver.getClassInfo(classId);
                return classInfo.readBytes(byteArray, true);
            }
            case 5 ->
            {
                int       classId   = byteArray.readVarInt();
                ClassInfo classInfo = classInfoResolver.getClassInfo(classId);
                return classInfo.readBytes(byteArray, false);
            }
            case 6 ->
            {
                int       classId    = byteArray.readVarInt();
                int       instanceId = byteArray.readVarInt();
                ClassInfo classInfo  = classInfoResolver.getClassInfo(classId);
                return classInfo.getTracking(instanceId);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public void registerClass(Class clazz)
    {
        classInfoResolver.registerClass(clazz);
    }
}
