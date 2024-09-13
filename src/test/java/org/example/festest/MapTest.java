package org.example.festest;

import com.jfirer.se2.JfireSE;
import lombok.Data;
import org.example.festest.data.BaseData;
import org.junit.Test;

import java.util.HashMap;

public class MapTest
{
    @Data
    public class MapDemo
    {
        private HashMap<Integer, BaseData> map = new HashMap<Integer, BaseData>();
    }

    @Test
    public void test()
    {
        MapDemo demo = new MapDemo();
        demo.getMap().put(1, new BaseData());
        JfireSE jfireSE   = JfireSE.staticRegisterClass(MapDemo.class).refTracking().build();
        byte[]  serialize = jfireSE.serialize(demo);
        jfireSE.deSerialize(serialize);
    }
}
