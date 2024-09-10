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
    private       int                      dyncmicClassId;
    /**
     * 用于存储序列化相关的 classInfo
     */
    private       ClassInfo[]              serializedClassInfos;
    /**
     * 用于存储逆序列化过程中的临时信息
     */
    private       ClassInfo[]              deSerializedClassInfos;
    private       SerializerFactory        serializerFactory = new SerializerFactory();
    private       ByteArray                byteArray         = new ByteArray(1000);
    private       ClassInfo                classInfoCache;
    private       Map<Class<?>, ClassInfo> classInfoMap      = new HashMap<>();

    public JfireSEImpl(boolean refTracking, StaticClasInfo[] staticClasInfos)
    {
        this.refTracking       = refTracking;
        this.staticClassId     = staticClasInfos.length - 1;
        serializedClassInfos   = new ClassInfo[staticClasInfos.length * 2];
        deSerializedClassInfos = new ClassInfo[serializedClassInfos.length];
        System.arraycopy(staticClasInfos, 0, serializedClassInfos, 0, staticClasInfos.length);
        System.arraycopy(staticClasInfos, 0, deSerializedClassInfos, 0, staticClasInfos.length);
        dyncmicClassId = staticClassId + 1;
    }

    public ClassInfo getForSerialize(Class<?> clazz)
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
        if (dyncmicClassId == serializedClassInfos.length)
        {
            ClassInfo[] tmp = new ClassInfo[serializedClassInfos.length * 2];
            System.arraycopy(serializedClassInfos, 0, tmp, 0, serializedClassInfos.length);
            serializedClassInfos = tmp;
        }
        DynamicClassInfo dynamicClassInfo = new DynamicClassInfo((short) dyncmicClassId, clazz, refTracking);
        serializedClassInfos[dyncmicClassId] = dynamicClassInfo;
        dyncmicClassId++;
        Serializer serializer = serializerFactory.getSerializer(clazz, this);
        dynamicClassInfo.setSerializer(serializer);
        classInfoCache = dynamicClassInfo;
        return dynamicClassInfo;
    }

    private void resetSerialized()
    {
        for (int i = staticClassId; i < dyncmicClassId; i++)
        {
            serializedClassInfos[i].reset();
        }
    }

    @Override
    public byte[] write(Object instance)
    {
        if (instance == null)
        {
            byteArray.put(JfireSE.NULL);
            byte[] array = byteArray.toArray();
            byteArray.clear();
            return array;
        }
        ClassInfo classInfo = getForSerialize(instance.getClass());
        classInfo.write(byteArray, instance);
        byte[] array = byteArray.toArray();
        byteArray.clear();
        resetSerialized();
        return array;
    }

    @Override
    public Object read(byte[] bytes)
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
                int       classId        = stream.readVarInt();
                ClassInfo classInfo      = getForDeSerialize(classNameBytes, classId);
                return classInfo.readWithTrack(stream);
            }
            case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
            {
                byte[] classNameBytes = stream.readBytesWithSizeEmbedded();
                int    classId        = stream.readVarInt();
                return getForDeSerialize(classNameBytes, classId).readWithoutTrack(stream);
            }
            case JfireSE.id_content_track ->
            {
                int       classId   = stream.readVarInt();
                ClassInfo classInfo = getForDeSerialize(classId);
                return classInfo.readWithTrack(stream);
            }
            case JfireSE.id_content_un_track ->
            {
                int       classId   = stream.readVarInt();
                ClassInfo classInfo = getForDeSerialize(classId);
                return classInfo.readWithoutTrack(stream);
            }
            default -> throw new RuntimeException("未知的序列化类型");
        }
    }

    public ClassInfo getForDeSerialize(byte[] classNameBytes, int classId)
    {
        try
        {
            Class<?>  clazz     = Class.forName(new String(classNameBytes, StandardCharsets.UTF_8));
            ClassInfo classInfo = getForSerialize(clazz);
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

    public ClassInfo getForDeSerialize(int classId)
    {
        return deSerializedClassInfos[classId];
    }
}
