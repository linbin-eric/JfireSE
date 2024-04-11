package org.example;

import com.jfirer.se.JfireSE;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FunctionTest
{
    @Test
    public void test()
    {
        JfireSE  jfireSE = new JfireSE();
        TestData data    = new TestData();
        //创建一个二进制数组容器，用于容纳序列化后的输出。容器大小会在需要时自动扩大，入参仅决定初始化大小。
        //执行序列化，会将序列化对象序列化到二进制数组容器之中。
        byte[]   bytes  = jfireSE.writeBytes(data);
        TestData result = (TestData) jfireSE.readBytes(bytes);
        assertTrue(result.equals(data));
    }

    @Test
    public void test2()
    {
        TestData data    = new TestData();
        JfireSE  jfireSE = new JfireSE();
        byte[]   bytes   = jfireSE.writeBytes(data);
        Object   o       = jfireSE.readBytes(bytes);
    }
}
