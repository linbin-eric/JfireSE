package org.example;

import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import io.fury.Fury;
import org.example.sm.TestDataSm;
import org.example.sm2.TestDataSm2;
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
        TestDataSm sm = new TestDataSm();
        sm.setC("dfdfdf");
        TestData testData = new TestData();
        testData.setB(true);
        testData.setTestDataSm(sm);
        testData.setTestDataSm2(new TestDataSm2());
        TestDataSm[] sms = new TestDataSm[2];
        sms[0] = new TestDataSm().setC("xx");
        testData.setSms(sms);
        JfireSE jfireSE = JfireSE.build();
        byte[]  bytes   = jfireSE.serialize(testData);
        Assert.assertEquals(testData, jfireSE.deSerialize(bytes));
        TestData read = (TestData) jfireSE.deSerialize(bytes);
        Assert.assertEquals("xx", read.getSms()[0].getC());
        Assert.assertNull(read.getSms()[1]);
    }

    @Test
    public void test3()
    {
        Fury fury = Fury.builder().requireClassRegistration(false).build();
        TestData[] data = new TestData[2];
        data[0] = new TestData();
        fury.serialize(data);
    }


}
