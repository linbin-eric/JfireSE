package cc.jfire.se2.serializer.impl.ObjectSerializer;

import cc.jfire.se2.ByteArray;
import cc.jfire.se2.JfireSE;

import java.lang.reflect.Field;

public class UnknowObjectFieldInfo extends FieldInfo
{
    private final JfireSE jfireSE;

    public UnknowObjectFieldInfo(Field field, JfireSE jfireSE)
    {
        super(field);
        this.jfireSE = jfireSE;
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        if (instance == null)
        {
            byteArray.put(JfireSE.NULL);
        }
        else
        {
            jfireSE.getOrCreateClassInfo(instance.getClass()).write(byteArray, instance);
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
            accessor.setObject(instance, jfireSE.readByUnderInstanceIdFlag(byteArray, flag));
        }
    }
}
