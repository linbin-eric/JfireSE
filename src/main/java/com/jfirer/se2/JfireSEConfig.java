package com.jfirer.se2;

import com.jfirer.se2.classinfo.StaticClasInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@Accessors(chain = true)
public class JfireSEConfig
{
    private        boolean          refTracking         = false;
    private        short            staticClassId       = 0;
    private        StaticClasInfo[] clasInfos           = new StaticClasInfo[10];
    private        Set<Class<?>>    set                 = new HashSet<>();
    private static Set<Class<?>>    NO_NEED_TRACK_CLASS = new HashSet<>();

    static
    {
        NO_NEED_TRACK_CLASS.add(Integer.class);
        NO_NEED_TRACK_CLASS.add(Long.class);
        NO_NEED_TRACK_CLASS.add(Short.class);
        NO_NEED_TRACK_CLASS.add(Byte.class);
        NO_NEED_TRACK_CLASS.add(Character.class);
        NO_NEED_TRACK_CLASS.add(Float.class);
        NO_NEED_TRACK_CLASS.add(Double.class);
        NO_NEED_TRACK_CLASS.add(Boolean.class);
        NO_NEED_TRACK_CLASS.add(String.class);
        NO_NEED_TRACK_CLASS.add(int[].class);
        NO_NEED_TRACK_CLASS.add(long[].class);
        NO_NEED_TRACK_CLASS.add(short[].class);
        NO_NEED_TRACK_CLASS.add(byte[].class);
        NO_NEED_TRACK_CLASS.add(char[].class);
        NO_NEED_TRACK_CLASS.add(float[].class);
        NO_NEED_TRACK_CLASS.add(double[].class);
        NO_NEED_TRACK_CLASS.add(boolean[].class);
        NO_NEED_TRACK_CLASS.add(Integer[].class);
        NO_NEED_TRACK_CLASS.add(Long[].class);
        NO_NEED_TRACK_CLASS.add(Byte[].class);
        NO_NEED_TRACK_CLASS.add(Boolean[].class);
        NO_NEED_TRACK_CLASS.add(Character[].class);
        NO_NEED_TRACK_CLASS.add(Double[].class);
        NO_NEED_TRACK_CLASS.add(Float[].class);
        NO_NEED_TRACK_CLASS.add(String[].class);
        NO_NEED_TRACK_CLASS.add(Date.class);
        NO_NEED_TRACK_CLASS.add(java.sql.Date.class);
        NO_NEED_TRACK_CLASS.add(Calendar.class);
    }

    public JfireSEConfig()
    {
        staticRegisterClass(int[].class);
        staticRegisterClass(long[].class);
        staticRegisterClass(byte[].class);
        staticRegisterClass(boolean[].class);
        staticRegisterClass(char[].class);
        staticRegisterClass(short[].class);
        staticRegisterClass(double[].class);
        staticRegisterClass(float[].class);
        staticRegisterClass(Integer[].class);
        staticRegisterClass(Long[].class);
        staticRegisterClass(Byte[].class);
        staticRegisterClass(Boolean[].class);
        staticRegisterClass(Character[].class);
        staticRegisterClass(Short[].class);
        staticRegisterClass(Double[].class);
        staticRegisterClass(Float[].class);
        staticRegisterClass(String[].class);
        staticRegisterClass(String.class);
        staticRegisterClass(Byte.class);
        staticRegisterClass(Integer.class);
        staticRegisterClass(Character.class);
        staticRegisterClass(Long.class);
        staticRegisterClass(Float.class);
        staticRegisterClass(Double.class);
        staticRegisterClass(Boolean.class);
        staticRegisterClass(Short.class);
        staticRegisterClass(ArrayList.class);
        staticRegisterClass(LinkedList.class);
        staticRegisterClass(HashMap.class);
        staticRegisterClass(LinkedHashMap.class);
        staticRegisterClass(ConcurrentHashMap.class);
        staticRegisterClass(TreeMap.class);
        staticRegisterClass(HashSet.class);
        staticRegisterClass(TreeSet.class);
        staticRegisterClass(LinkedHashSet.class);
        staticRegisterClass(ConcurrentSkipListSet.class);
        staticRegisterClass(Date.class);
        staticRegisterClass(java.sql.Date.class);
    }

    public JfireSEConfig refTracking()
    {
        refTracking = true;
        return this;
    }

    public JfireSEConfig staticRegisterClass(Class<?> clazz)
    {
        if (set.add(clazz))
        {
            return this;
        }
        else
        {
            throw new IllegalArgumentException("重复添加");
        }
    }

    public JfireSE build()
    {
        StaticClasInfo[] array = set.stream().map(this::resolve).toArray(StaticClasInfo[]::new);
        return new JfireSEImpl(refTracking, array);
    }

    private StaticClasInfo resolve(Class<?> clazz)
    {
        StaticClasInfo staticClasInfo = new StaticClasInfo(staticClassId, clazz, NO_NEED_TRACK_CLASS.contains(clazz) ? false : refTracking);
        staticClassId++;
        return staticClasInfo;
    }
}
