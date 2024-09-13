package com.jfirer.se2.serializer.impl.jdk;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

public class DateSerializer implements Serializer
{
    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        byteArray.writeVarLong(((java.util.Date) instance).getTime());
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        return new java.util.Date(byteArray.readVarLong());
    }
}
