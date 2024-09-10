package org.example;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.Assert;
import org.junit.Test;

public class FunctionTest
{
    @Test
    public void test()
    {
        ByteArray byteArray = new ByteArray(1000);
        byteArray.writeString("你好");
        byteArray.writeString("nihao");
        byteArray.writeVarInt(20);
        byteArray.writeVarLong(13453242);
        Assert.assertEquals("你好", byteArray.readString());
        Assert.assertEquals("nihao", byteArray.readString());
        Assert.assertEquals(20, byteArray.readVarInt());
        Assert.assertEquals(13453242, byteArray.readVarLong());
    }

    @Test
    public void test2()
    {
        TestData testData = new TestData();
        testData.setB(true);
        JfireSE jfireSE = JfireSE.build();
        byte[]  bytes   = jfireSE.write(testData);
        Assert.assertEquals(testData, jfireSE.read(bytes));
    }

    @Test
    public void test3()
    {
        Fury fury = Fury.builder().withLanguage(Language.JAVA)//
                        .requireClassRegistration(false)//
                        .withRefTracking(true).build();
        TestData data       = new TestData();
        byte[]   serialize  = fury.serialize(data);
        byte[]   serialize1 = fury.serialize(data);
        Assert.assertArrayEquals(serialize, serialize1);
    }
}
