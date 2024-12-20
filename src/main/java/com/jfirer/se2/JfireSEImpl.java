package com.jfirer.se2;

import com.jfirer.se2.classinfo.ClassInfo;
import com.jfirer.se2.classinfo.DynamicClassInfo;
import com.jfirer.se2.classinfo.RegisterClasInfo;
import com.jfirer.se2.serializer.Serializer;
import com.jfirer.se2.serializer.SerializerFactory;

import java.util.*;

public class JfireSEImpl implements JfireSE
{
    private final boolean                  refTracking;
    private       int                      dynamicClassId;
    /**
     * 用于存储序列化相关的 classInfo
     */
    private       ClassInfo[]              storedClassInfos;
    /**
     * 用于存储逆序列化过程中的临时信息。
     * 这些信息不需要清理，因为每一次逆序列化，对首次出现的类都会有 classId，可以用这个 classId 直接在数组中设置元素。这样后续通过下标的到的值都是正确的。
     * 因此这个数组中的元素部分是本次序列化中正确的，部分是之前残余的。但是之前残余的本次都不会被使用到的，因此该数组不需要清理。
     */
    private       ClassInfo[]              deSerializedClassInfos;
    private final ByteArray                byteArray           = new ByteArray(1000);
    private       ClassInfo                classInfoCache;
    private final Map<Class<?>, ClassInfo> classInfoMap        = new IdentityHashMap<>();
    private final SerializerFactory        serializerFactory   = new SerializerFactory(this);
    private final Map<String, Class<?>>    classNameBytesCache = new HashMap<>();
    private final List<ClassInfo>          scheduleForCleans   = new ArrayList<>();

    public JfireSEImpl(boolean refTracking, RegisterClasInfo[] registerClasInfos)
    {
        this.refTracking = refTracking;
        int staticClassId = registerClasInfos.length - 1;
        storedClassInfos       = new ClassInfo[registerClasInfos.length * 2];
        deSerializedClassInfos = new ClassInfo[storedClassInfos.length];
        System.arraycopy(registerClasInfos, 0, storedClassInfos, 0, registerClasInfos.length);
        System.arraycopy(registerClasInfos, 0, deSerializedClassInfos, 0, registerClasInfos.length);
        dynamicClassId = staticClassId + 1;
        for (RegisterClasInfo each : registerClasInfos)
        {
            each.setSerializer(serializerFactory.getSerializer(each.getClazz()));
            each.setJfireSE(this);
        }
    }

    @Override
    public ClassInfo getOrCreateClassInfo(Class<?> clazz)
    {
        if (classInfoCache != null && classInfoCache.getClazz() == clazz)
        {
            return classInfoCache;
        }
        ClassInfo classInfo = classInfoMap.get(clazz);
        if (classInfo != null)
        {
            classInfoCache = classInfo;
            return classInfo;
        }
        if (dynamicClassId == storedClassInfos.length)
        {
            ClassInfo[] tmp = new ClassInfo[storedClassInfos.length * 2];
            System.arraycopy(storedClassInfos, 0, tmp, 0, storedClassInfos.length);
            storedClassInfos = tmp;
        }
        DynamicClassInfo dynamicClassInfo = new DynamicClassInfo((short) dynamicClassId, clazz, refTracking);
        dynamicClassInfo.setJfireSE(this);
        storedClassInfos[dynamicClassId] = dynamicClassInfo;
        dynamicClassId++;
        Serializer serializer = serializerFactory.getSerializer(clazz);
        dynamicClassInfo.setSerializer(serializer);
        classInfoCache = dynamicClassInfo;
        classInfoMap.put(clazz, dynamicClassInfo);
        return dynamicClassInfo;
    }

    private void clean()
    {
        for (ClassInfo each : scheduleForCleans)
        {
            each.clean();
        }
        byteArray.clear();
        scheduleForCleans.clear();
    }

    @Override
    public byte[] serialize(Object instance)
    {
        byteArray.clear();
        if (instance == null)
        {
            byteArray.put(JfireSE.NULL);
            byte[] array = byteArray.toArray();
            byteArray.clear();
            return array;
        }
        ClassInfo classInfo = getOrCreateClassInfo(instance.getClass());
        classInfo.write(byteArray, instance);
        byte[] array = byteArray.toArray();
        clean();
        return array;
    }

    @Override
    public Object deSerialize(byte[] bytes)
    {
        byteArray.resetFor(bytes);
        byte b = byteArray.get();
        if (b == JfireSE.NULL)
        {
            return null;
        }
        Object result = switch (b)
        {
            case JfireSE.NAME_ID_CONTENT_TRACK ->
            {
                String    name      = byteArray.readString();
                int       classId   = byteArray.readPositiveVarInt();
                ClassInfo classInfo = find(name, classId);
                yield classInfo.readWithTrack(byteArray);
            }
            case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
            {
                String className = byteArray.readString();
                int    classId   = byteArray.readPositiveVarInt();
                yield find(className, classId).readWithoutTrack(byteArray);
            }
            case JfireSE.ID_CONTENT_TRACK ->
            {
                int       classId   = byteArray.readPositiveVarInt();
                ClassInfo classInfo = find(classId);
                yield classInfo.readWithTrack(byteArray);
            }
            case JfireSE.ID_CONTENT_UN_TRACK ->
            {
                int       classId   = byteArray.readPositiveVarInt();
                ClassInfo classInfo = find(classId);
                yield classInfo.readWithoutTrack(byteArray);
            }
            default -> throw new RuntimeException("未知的序列化类型");
        };
        clean();
        return result;
    }

    @Override
    public ClassInfo find(String className, int classId)
    {
        Class<?> aClass = classNameBytesCache.get(className);
        if (aClass == null)
        {
            try
            {
                aClass = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            classNameBytesCache.put(className, aClass);
        }
        ClassInfo classInfo = getOrCreateClassInfo(aClass);
        if (classId >= deSerializedClassInfos.length)
        {
            int newLen = deSerializedClassInfos.length * 2;
            newLen = newLen > classId ? newLen : classId + 1;
            ClassInfo[] tmp = new ClassInfo[newLen];
            System.arraycopy(deSerializedClassInfos, 0, tmp, 0, deSerializedClassInfos.length);
            deSerializedClassInfos          = tmp;
        }
        deSerializedClassInfos[classId] = classInfo;
        return classInfo;
    }

    @Override
    public ClassInfo find(int classId)
    {
        return deSerializedClassInfos[classId];
    }

    @Override
    public Object readByUnderInstanceIdFlag(ByteArray byteArray, byte flag)
    {
        switch (flag)
        {
            case JfireSE.NAME_ID_CONTENT_TRACK ->
            {
                String    className = byteArray.readString();
                int       classId   = byteArray.readPositiveVarInt();
                ClassInfo classInfo = find(className, classId);
                return refTracking ? classInfo.readWithTrack(byteArray) : classInfo.readWithoutTrack(byteArray);
            }
            case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
            {
                String className = byteArray.readString();
                int    classId   = byteArray.readPositiveVarInt();
                return find(className, classId).readWithoutTrack(byteArray);
            }
            case JfireSE.ID_INSTANCE_ID ->
            {
                return find(byteArray.readPositiveVarInt()).getInstanceById(byteArray.readPositiveVarInt());
            }
            case JfireSE.ID_CONTENT_TRACK ->
            {
                int classId = byteArray.readPositiveVarInt();
                return find(classId).readWithTrack(byteArray);
            }
            case JfireSE.ID_CONTENT_UN_TRACK ->
            {
                int classId = byteArray.readPositiveVarInt();
                return find(classId).readWithoutTrack(byteArray);
            }
            default -> throw new RuntimeException("未知的序列化类型");
        }
    }

    @Override
    public void scheduleForClean(ClassInfo classInfo)
    {
        scheduleForCleans.add(classInfo);
    }
}
