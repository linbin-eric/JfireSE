package org.example;

import com.jfirer.se.ByteArray;
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
}
