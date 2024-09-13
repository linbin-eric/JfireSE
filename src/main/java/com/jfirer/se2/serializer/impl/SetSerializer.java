package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class SetSerializer implements Serializer
{
    private              JfireSE jfireSE;
    private              int     type;
    private static final int     HASH_SET                 = 1;
    private static final int     LINKED_HASH_SET          = 2;
    private static final int     TREE_SET                 = 3;
    private static final int     CONCURRENT_SKIP_LIST_SET = 4;

    public SetSerializer(Class<?> type, JfireSE jfireSE)
    {
        this.jfireSE = jfireSE;
        if (type == HashSet.class)
        {
            this.type = HASH_SET;
        }
        else if (type == LinkedHashSet.class)
        {
            this.type = LINKED_HASH_SET;
        }
        else if (type == TreeSet.class)
        {
            this.type = TREE_SET;
        }
        else if (type == ConcurrentSkipListSet.class)
        {
            this.type = CONCURRENT_SKIP_LIST_SET;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        Set<?> collection = (Set<?>) instance;
        int    size       = collection.size();
        byteArray.writePositiveVarInt(size);
        for (Object each : collection)
        {
            if (each == null)
            {
                byteArray.put(JfireSE.NULL);
            }
            else
            {
                jfireSE.getOrCreateClassInfo(each.getClass()).write(byteArray, each);
            }
        }
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        Set<Object> set = switch (type)
        {
            case HASH_SET -> new HashSet<>();
            case LINKED_HASH_SET -> new LinkedHashSet<>();
            case TREE_SET -> new TreeSet<>();
            case CONCURRENT_SKIP_LIST_SET -> new ConcurrentSkipListSet<>();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        int size = byteArray.readPositiveVarInt();
        if (refTracking != null)
        {
            refTracking.addTracking(set);
        }
        for (int i = 0; i < size; i++)
        {
            byte b = byteArray.get();
            if (b == JfireSE.NULL)
            {
                set.add(null);
            }
            else
            {
                set.add(jfireSE.readByUnderInstanceIdFlag(byteArray, b));
            }
        }
        return set;
    }
}
