package com.jfirer.se2;

import com.jfirer.se2.classinfo.ClassInfo;
import com.jfirer.se2.classinfo.DynamicClassInfo;
import com.jfirer.se2.classinfo.StaticClasInfo;
import com.jfirer.se2.serializer.Serializer;
import com.jfirer.se2.serializer.SerializerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JfireSEImpl implements JfireSE
{
    private final boolean                  refTracking;
    private final int                      staticClassId;
    private       int                      dynamicClassId;
    /**
     * 用于存储序列化相关的 classInfo
     */
    private       ClassInfo[]              serializedClassInfos;
    /**
     * 用于存储逆序列化过程中的临时信息
     */
    private       ClassInfo[]              deSerializedClassInfos;
    private       ByteArray                byteArray         = new ByteArray(1000);
    private       ClassInfo                classInfoCache;
    private       Map<Class<?>, ClassInfo> classInfoMap      = new HashMap<>();
    private       SerializerFactory        serializerFactory = new SerializerFactory(this);
    private       Map<byte[], ClassInfo>   classInfoCacheMap = new HashMap<>();

    public JfireSEImpl(boolean refTracking, StaticClasInfo[] staticClasInfos)
    {
        for (StaticClasInfo each : staticClasInfos)
        {
            each.setSerializer(serializerFactory.getSerializer(each.getClazz()));
        }
        this.refTracking       = refTracking;
        this.staticClassId     = staticClasInfos.length - 1;
        serializedClassInfos   = new ClassInfo[staticClasInfos.length * 2];
        deSerializedClassInfos = new ClassInfo[serializedClassInfos.length];
        System.arraycopy(staticClasInfos, 0, serializedClassInfos, 0, staticClasInfos.length);
        System.arraycopy(staticClasInfos, 0, deSerializedClassInfos, 0, staticClasInfos.length);
        dynamicClassId = staticClassId + 1;
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
        if (dynamicClassId == serializedClassInfos.length)
        {
            ClassInfo[] tmp = new ClassInfo[serializedClassInfos.length * 2];
            System.arraycopy(serializedClassInfos, 0, tmp, 0, serializedClassInfos.length);
            serializedClassInfos = tmp;
        }
        DynamicClassInfo dynamicClassInfo = new DynamicClassInfo((short) dynamicClassId, clazz, refTracking);
        serializedClassInfos[dynamicClassId] = dynamicClassInfo;
        dynamicClassId++;
        Serializer serializer = serializerFactory.getSerializer(clazz);
        dynamicClassInfo.setSerializer(serializer);
        classInfoCache = dynamicClassInfo;
        return dynamicClassInfo;
    }

    private void resetSerialized()
    {
        for (int i = staticClassId; i < dynamicClassId; i++)
        {
            serializedClassInfos[i].reset();
        }
    }

    @Override
    public byte[] serialize(Object instance)
    {
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
        byteArray.clear();
        resetSerialized();
        return array;
    }

    @Override
    public Object deSerialize(byte[] bytes)
    {
        ByteArray stream = new ByteArray(bytes);
        byte      b      = stream.get();
        if (b == JfireSE.NULL)
        {
            return null;
        }
        switch (b)
        {
            case JfireSE.NAME_ID_CONTENT_TRACK ->
            {
                byte[]    classNameBytes = stream.readBytesWithSizeEmbedded();
                int       classId        = stream.readPositiveVarInt();
                ClassInfo classInfo      = find(classNameBytes, classId);
                return classInfo.readWithTrack(stream);
            }
            case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
            {
                byte[] classNameBytes = stream.readBytesWithSizeEmbedded();
                int    classId        = stream.readPositiveVarInt();
                return find(classNameBytes, classId).readWithoutTrack(stream);
            }
            case JfireSE.ID_CONTENT_TRACK ->
            {
                int       classId   = stream.readPositiveVarInt();
                ClassInfo classInfo = find(classId);
                return classInfo.readWithTrack(stream);
            }
            case JfireSE.ID_CONTENT_UN_TRACK ->
            {
                int       classId   = stream.readPositiveVarInt();
                ClassInfo classInfo = find(classId);
                return classInfo.readWithoutTrack(stream);
            }
            default -> throw new RuntimeException("未知的序列化类型");
        }
    }

    @Override
    public ClassInfo find(byte[] classNameBytes, int classId)
    {
        try
        {
            Class<?>  clazz     = Class.forName(new String(classNameBytes, StandardCharsets.UTF_8));
            ClassInfo classInfo = getOrCreateClassInfo(clazz);
            if (deSerializedClassInfos == null)
            {
                deSerializedClassInfos          = new ClassInfo[classId + 1];
                deSerializedClassInfos[classId] = classInfo;
            }
            else if (classId >= deSerializedClassInfos.length)
            {
                int newLen = deSerializedClassInfos.length * 2;
                newLen = newLen > classId ? newLen : classId + 1;
                ClassInfo[] tmp = new ClassInfo[newLen];
                System.arraycopy(deSerializedClassInfos, 0, tmp, 0, deSerializedClassInfos.length);
                deSerializedClassInfos          = tmp;
                deSerializedClassInfos[classId] = classInfo;
            }
            else
            {
                deSerializedClassInfos[classId] = classInfo;
            }
            return classInfo;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
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
                byte[]    classNameBytes = byteArray.readBytesWithSizeEmbedded();
                int       classId        = byteArray.readPositiveVarInt();
                ClassInfo classInfo      = find(classNameBytes, classId);
                return refTracking ? classInfo.readWithTrack(byteArray) : classInfo.readWithoutTrack(byteArray);
            }
            case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
            {
                byte[] classNameBytes = byteArray.readBytesWithSizeEmbedded();
                int    classId        = byteArray.readPositiveVarInt();
                return find(classNameBytes, classId).readWithoutTrack(byteArray);
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
}
