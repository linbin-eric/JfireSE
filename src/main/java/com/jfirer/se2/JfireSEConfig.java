package com.jfirer.se2;

import com.jfirer.se2.classinfo.StaticClasInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true)
public class JfireSEConfig
{
    private boolean          refTracking   = false;
    private boolean          useCompile    = false;
    private short            staticClassId = 0;
    private StaticClasInfo[] clasInfos     = new StaticClasInfo[10];
    private List<Class<?>>   list          = new LinkedList<>();
    private Set<Class<?>>    set           = new HashSet<>();

    public JfireSEConfig()
    {
        staticRegisterClass(ArrayList.class);
        staticRegisterClass(HashSet.class);
        staticRegisterClass(HashMap.class);
        staticRegisterClass(LinkedList.class);
    }

    public JfireSEConfig useCompile()
    {
        useCompile = true;
        return this;
    }

    public JfireSEConfig refTracking()
    {
        refTracking = true;
        return this;
    }

    public JfireSEConfig staticRegisterClass(Class<?> clazz)
    {
        if (set.add(clazz))
        {
            list.add(clazz);
            return this;
        }
        else
        {
            throw new IllegalArgumentException("重复添加");
        }
    }

    public JfireSE build()
    {
        StaticClasInfo[] array = list.stream().map(this::resolve).toArray(StaticClasInfo[]::new);
        return new JfireSEImpl(refTracking, array);
    }

    private StaticClasInfo resolve(Class<?> clazz)
    {
        StaticClasInfo staticClasInfo = new StaticClasInfo(staticClassId, clazz, refTracking);
        staticClassId++;
        return staticClasInfo;
    }
}
