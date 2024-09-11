package org.example;

import lombok.Data;
import lombok.experimental.Accessors;
import org.example.sm.TestDataSm;
import org.example.sm2.TestDataSm2;

@Data
@Accessors(chain = true)
public class TestData
{
    private int          a = 213212312;
    private boolean      b = false;
    private char         c = 'c';
    private byte         d = 0x11;
    private short        e = 24;
    private long         f = 1213124131312321L;
    private double       g = 231321.2132;
    private float        h = (float) 4986.2;
    private String      i = "123452312316789a";
    private TestDataSm   testDataSm;
    private TestDataSm2  testDataSm2;
    private TestDataSm[] sms;
//    private int[]     j = new int[]{1, 2, 4, 5};
//    private boolean[] k = new boolean[]{true, false, true, false, false, false, true};
//    private char[]    l = new char[]{'a', 'v', 'q', 'j', 'h', 'e', 'f'};
//    private byte[]    m = new byte[]{0x32, 0x12, 0x34, (byte) 0x96};
//    private short[]   n = new short[]{3, 8, 213, 451, 312, 45};
//    private long[]    o = new long[]{12313131313l, 524141431313l, 3131231231425l, 1313123121l};
//    private double[]  p = new double[]{6468613646.48646d, 4646.456d, 546864648867.466d};
//    private float[]   q = new float[]{46486.2f, 49849.2f, 646854.6f};
//    private String[]  r = new String[]{"abcdf12345", "abdfcgf12323"};
}
