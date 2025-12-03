package org.example;

import cc.jfire.se2.JfireSE;
import org.example.sm.TestDataSm;
import org.example.sm2.TestDataSm2;
import org.junit.Test;

public class Profile
{
    JfireSE  jfireSE   = JfireSE.config().refTracking().build();
    TestData data      = new TestData().setTestDataSm(new TestDataSm()).setTestDataSm2(new TestDataSm2());
    byte[]   serialize = jfireSE.serialize(data);
    Object   unuse     = jfireSE.deSerialize(serialize);

    @Test
    public void test()
    {
        for (int i = 0; i < 30000000; i++)
        {
            jfireSE.deSerialize(serialize);
        }
    }
}
