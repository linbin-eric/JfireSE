package cc.jfire.se2.serializer.impl.jdk;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

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
