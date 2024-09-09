package com.jfirer.se2.serializer.impl.ObjectSerializer;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import com.jfirer.se2.JfireSEImpl;
import com.jfirer.se2.classinfo.ClassInfo;

import java.lang.reflect.Field;

public class VariableFieldInfo extends FieldInfo
{
    private final JfireSEImpl jfireSE;
    private       ClassInfo   classInfo;

    public VariableFieldInfo(Field field, JfireSEImpl jfireSE)
    {
        super(ReflectUtil.getClassId(field.getType()), new ValueAccessor(field));
        classInfo    = jfireSE.getForSerialize(field.getType());
        this.jfireSE = jfireSE;
    }

    @Override
    public void write(ByteArray byteArray, Object instance)
    {
        Object obj = accessor.get(instance);
        if (obj == null)
        {
            byteArray.put(JfireSE.NULL);
        }
        else
        {
            Class<?> objClass = obj.getClass();
            if (objClass == classInfo.getClazz())
            {
                classInfo.write(byteArray, obj);
            }
            else
            {
                classInfo = jfireSE.getForSerialize(objClass);
                classInfo.write(byteArray, instance);
            }
        }
    }
}
