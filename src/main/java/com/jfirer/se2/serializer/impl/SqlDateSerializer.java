package com.jfirer.se2.serializer.impl;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.classinfo.RefTracking;
import com.jfirer.se2.serializer.Serializer;

public class SqlDateSerializer implements Serializer
{
    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        byteArray.writeVarLong(((java.sql.Date) instance).getTime());
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        return new java.sql.Date(byteArray.readVarLong());
    }
}
