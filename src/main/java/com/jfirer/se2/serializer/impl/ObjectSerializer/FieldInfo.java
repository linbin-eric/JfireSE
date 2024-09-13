package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.se2.ByteArray;
import lombok.Data;

import java.lang.reflect.Field;

@Data
public abstract class FieldInfo
{
    protected final      int           classId;
    protected final      ValueAccessor accessor;
    private static final CompileHelper COMPILE_HELPER = new CompileHelper();
    protected            Field         field;

    public FieldInfo(Field field)
    {
        this.classId  = ReflectUtil.getClassId(field.getType());
        this.accessor = new ValueAccessor(field);
        this.field    = field;
    }

    public abstract void write(ByteArray byteArray, Object instance);

    public abstract void read(ByteArray byteArray, Object instance);

    public void init()
    {
    }
}
