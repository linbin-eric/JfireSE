package org.example;

import io.github.karlatemp.unsafeaccessor.Unsafe;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

public class Demo
{
    private long l = 0x0102030405060708L;

    @Test
    public void test() throws IOException, NoSuchFieldException
    {
        Unsafe  unsafe = Unsafe.getUnsafe();
        boolean isBig  = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
        System.out.println(isBig);
        Field   l1     = Demo.class.getDeclaredField("l");
        long    offset = unsafe.objectFieldOffset(l1);
        System.out.println(unsafe.getLong(this, offset));
        System.out.println(l);
        System.out.println((byte)(l>>56));
        System.out.println(unsafe.getByte(this,offset));
        System.out.println(unsafe.getByte(this,offset+1));
    }
}
