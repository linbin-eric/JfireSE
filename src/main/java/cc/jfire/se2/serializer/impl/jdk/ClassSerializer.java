package cc.jfire.se2.serializer.impl.jdk;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

public class ClassSerializer implements Serializer
{
    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        Class<?> clz = (Class<?>) instance;
        byteArray.writeString(clz.getName());
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        String name = byteArray.readString();
        try
        {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
