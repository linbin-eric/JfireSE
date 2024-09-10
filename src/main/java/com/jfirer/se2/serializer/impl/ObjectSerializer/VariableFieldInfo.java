package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.classinfo.ClassInfo;

import java.lang.reflect.Field;

public class VariableFieldInfo extends FieldInfo
{
    private final JfireSEImpl jfireSE;
    private       ClassInfo   classInfo;
    private final ClassInfo   firstClassInfo;

    public VariableFieldInfo(Field field, JfireSEImpl jfireSE)
    {
        super(field);
        classInfo    = jfireSE.getForSerialize(field.getType());
        this.jfireSE = jfireSE;
        if (field.getType().isInterface())
        {
            firstClassInfo = null;
        }
        else
        {
            firstClassInfo = classInfo;
        }
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        Object obj = accessor.get(instance);
        if (obj == null)
        {
            byteArray.put(JfireSE.NULL);
        }
        else
        {
            Class<?> objClass = obj.getClass();
            if (objClass == classInfo.getClazz())
            {
                if (classInfo == firstClassInfo)
                {
                    classInfo.writeKnownClazz(byteArray, obj);
                }
                else
                {
                    classInfo.write(byteArray, obj);
                }
            }
            else
            {
                classInfo = jfireSE.getForSerialize(objClass);
                if (classInfo == firstClassInfo)
                {
                    classInfo.writeKnownClazz(byteArray, obj);
                }
                else
                {
                    classInfo.write(byteArray, obj);
                }
            }
        }
    }

    @Override
    public void read(ByteArray byteArray, Object instance)
    {
        byte flag = byteArray.get();
        if (flag == JfireSE.NULL)
        {
            accessor.setObject(instance, null);
        }
        else
        {
            switch (flag)
            {
                case JfireSE.NAME_ID_CONTENT_TRACK ->
                {
                    byte[] classNameBytes = byteArray.readBytesWithSizeEmbedded();
                    int    classId        = byteArray.readVarInt();
                    classInfo = jfireSE.getForDeSerialize(classNameBytes, classId);
                    Object property = classInfo.readWithTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.NAME_ID_CONTENT_UN_TRACK ->
                {
                    byte[] classNameBytes = byteArray.readBytesWithSizeEmbedded();
                    int    classId   = byteArray.readVarInt();
                    classInfo = jfireSE.getForDeSerialize(classNameBytes, classId);
                    Object property = classInfo.readWithoutTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.ID_INSTANCE_ID ->
                {
                    int classId    = byteArray.readVarInt();
                    int instanceId = byteArray.readVarInt();
                    classInfo = jfireSE.getForDeSerialize(classId);
                    Object proeprty = classInfo.getInstanceById(instanceId);
                    accessor.setObject(instance, proeprty);
                }
                case JfireSE.ID_CONTENT_TRACK ->
                {
                    int classId = byteArray.readVarInt();
                    classInfo = jfireSE.getForDeSerialize(classId);
                    Object property = classInfo.readWithTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.ID_CONTENT_UN_TRACK ->
                {
                    int classId = byteArray.readVarInt();
                    classInfo = jfireSE.getForDeSerialize(classId);
                    Object property = classInfo.readWithoutTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.INSTANCE_ID ->
                {
                    int    instanceId = byteArray.readVarInt();
                    Object property   = firstClassInfo.getInstanceById(instanceId);
                    accessor.setObject(instance, property);
                }
                case JfireSE.CONTENT_TRACK ->
                {
                    Object property = firstClassInfo.readWithTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                case JfireSE.CONTENT_UN_TRACK ->
                {
                    Object property = firstClassInfo.readWithoutTrack(byteArray);
                    accessor.setObject(instance, property);
                }
                default -> throw new RuntimeException("flag:" + flag);
            }
        }
    }
}
