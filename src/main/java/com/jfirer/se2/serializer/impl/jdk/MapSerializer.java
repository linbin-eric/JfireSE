package com.jfirer.se2.serializer.impl.jdk;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class MapSerializer implements Serializer
{
    private             JfireSE jfireSE;
    private             int     type;
    public static final int     HASHMAP           = 1;
    public static final int     TREEMAP           = 2;
    public static final int     LINKEDHASHMAP     = 3;
    public static final int     CONCURRENTHASHMAP = 4;

    public MapSerializer(JfireSE jfireSE, Class<? extends Map> type)
    {
        this.jfireSE = jfireSE;
        if (type.equals(HashMap.class))
        {
            this.type = HASHMAP;
        }
        else if (type.equals(TreeMap.class))
        {
            this.type = TREEMAP;
        }
        else if (type.equals(LinkedHashMap.class))
        {
            this.type = LINKEDHASHMAP;
        }
        else if (type.equals(ConcurrentHashMap.class))
        {
            this.type = CONCURRENTHASHMAP;
        }
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        Map<?, ?> map  = (Map) instance;
        int       size = map.size();
        byteArray.writePositiveVarInt(size);
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            Object key   = entry.getKey();
            Object value = entry.getValue();
            jfireSE.getOrCreateClassInfo(key.getClass()).write(byteArray, key);
            if (value == null)
            {
                byteArray.put(JfireSE.NULL);
            }
            else
            {
                jfireSE.getOrCreateClassInfo(value.getClass()).write(byteArray, value);
            }
        }
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        Map<Object, Object> map = switch (type)
        {
            case HASHMAP -> new HashMap<>();
            case TREEMAP -> new TreeMap<>();
            case LINKEDHASHMAP -> new LinkedHashMap<>();
            case CONCURRENTHASHMAP -> new ConcurrentHashMap<>();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        int size = byteArray.readPositiveVarInt();
        for (int i = 0; i < size; i++)
        {
            Object key;
            byte   flag = byteArray.get();
            if (flag >= JfireSE.NAME_ID_CONTENT_TRACK && flag <= JfireSE.ID_CONTENT_UN_TRACK)
            {
                key = jfireSE.readByUnderInstanceIdFlag(byteArray, flag);
            }
            else
            {
                throw new IllegalStateException("Unexpected value: " + flag);
            }
            Object value;
            byte   b = byteArray.get();
            if (b == JfireSE.NULL)
            {
                value = null;
            }
            else if (b >= JfireSE.NAME_ID_CONTENT_TRACK && b <= JfireSE.ID_CONTENT_UN_TRACK)
            {
                value = jfireSE.readByUnderInstanceIdFlag(byteArray, b);
            }
            else
            {
                throw new IllegalStateException("Unexpected value: " + b);
            }
            map.put(key, value);
        }
        return map;
    }
}
