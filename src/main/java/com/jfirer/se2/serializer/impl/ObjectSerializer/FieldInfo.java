package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se2.ByteArray;

public abstract class FieldInfo
{
    protected int           classId;
    protected ValueAccessor accessor;

    public FieldInfo(int classId, ValueAccessor accessor)
    {
        this.classId  = classId;
        this.accessor = accessor;
    }

    public abstract void write(ByteArray byteArray, Object instance);
}
