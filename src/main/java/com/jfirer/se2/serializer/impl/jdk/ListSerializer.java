package com.jfirer.se2.serializer.impl.jdk;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.jfirer.se2.JfireSE.*;

public class ListSerializer implements Serializer
{
    private              JfireSE jfireSE;
    private              int     type;
    private static final int     ARRAY_LIST  = 1;
    private static final int     LINKED_LIST = 2;

    public ListSerializer(JfireSE jfireSE, Class<?> type)
    {
        this.jfireSE = jfireSE;
        this.type    = type == ArrayList.class ? ARRAY_LIST : LINKED_LIST;
    }

    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        List<?> list = (List<?>) instance;
        int     size = list.size();
        byteArray.writePositiveVarInt(size);
        for (Object each : list)
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
        int          size = byteArray.readPositiveVarInt();
        List<Object> list = type == ARRAY_LIST ? new ArrayList<>() : new LinkedList<>();
        if (refTracking != null)
        {
            refTracking.addTracking(list);
        }
        for (int i = 0; i < size; i++)
        {
            byte flag = byteArray.get();
            if (flag == JfireSE.NULL)
            {
                list.add(null);
            }
            else
            {
                if (flag >= NAME_ID_CONTENT_TRACK && flag <= ID_CONTENT_UN_TRACK)
                {
                    list.add(jfireSE.readByUnderInstanceIdFlag(byteArray, flag));
                }
                else
                {
                    throw new IllegalStateException("Unexpected value: " + flag);
                }
            }
        }
        return list;
    }
}
