package cc.jfire.se2.serializer.impl.ObjectSerializer;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;
import cc.jfire.baseutil.smc.compiler.CompileHelper;
import cc.jfire.se2.ByteArray;
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
        this.accessor = ValueAccessor.standard(field);
        this.field    = field;
    }

    public abstract void write(ByteArray byteArray, Object instance);

    public abstract void read(ByteArray byteArray, Object instance);

    public void init()
    {
    }
}
