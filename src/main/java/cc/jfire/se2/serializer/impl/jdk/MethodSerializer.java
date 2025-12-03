package cc.jfire.se2.serializer.impl.jdk;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.se2.ByteArray;
import cc.jfire.se2.classinfo.RefTracking;
import cc.jfire.se2.serializer.Serializer;

import java.lang.reflect.Method;

public class MethodSerializer implements Serializer
{
    @Override
    public void writeBytes(ByteArray byteArray, Object instance)
    {
        Method method = (Method) instance;
        byteArray.writeString(method.getDeclaringClass().getName());
        byteArray.writeString(method.getName());
        byteArray.writePositiveVarInt(method.getParameterCount());
        for (Class<?> each : method.getParameterTypes())
        {
            byteArray.writeString(each.getName());
        }
    }

    @Override
    public Object read(ByteArray byteArray, RefTracking refTracking)
    {
        String className      = byteArray.readString();
        String methodName     = byteArray.readString();
        int    parameterCount = byteArray.readPositiveVarInt();
        if (parameterCount == 0)
        {
            try
            {
                return Class.forName(className).getDeclaredMethod(methodName);
            }
            catch (Throwable e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
        else
        {
            try
            {
                Class<?>[] parameterTypes = new Class[parameterCount];
                for (int i = 0; i < parameterCount; i++)
                {
                    parameterTypes[i] = Class.forName(byteArray.readString());
                }
                return Class.forName(className).getDeclaredMethod(methodName, parameterTypes);
            }
            catch (Throwable e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
    }
}
