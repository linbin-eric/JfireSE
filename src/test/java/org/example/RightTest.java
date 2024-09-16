package org.example;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.jfirer.se2.ByteArray;
import com.jfirer.se2.JfireSE;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.fury.Fury;
import org.example.festest.Home;
import org.example.festest.User;
import org.example.festest.data.*;
import org.example.festest.data.Person;
import org.example.sm.TestDataSm;
import org.example.sm2.TestDataSm2;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

@Slf4j
public class RightTest
{
    @Data
    public class MapDemo
    {
        private HashMap<Integer, BaseData> map = new HashMap<Integer, BaseData>();
    }

    @Test
    public void baseTypeTest() throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException, ClassNotFoundException, InstantiationException
    {
        // 创建需要序列化的对象
        BaseData baseData  = new BaseData(1);
        JfireSE  jfireSE   = JfireSE.config().refTracking().staticRegisterClass(baseData.getClass()).build();
        byte[]   serialize = jfireSE.serialize(baseData);
        // 传入二进制buffer对象，读取其中的 数据并且反序列化成对象
        BaseData result = (BaseData) jfireSE.deSerialize(serialize);
        assertEquals(result.getA(), baseData.getA());
        assertEquals(result.isB(), baseData.isB());
        assertEquals(result.getC(), baseData.getC());
        assertEquals(result.getD(), baseData.getD());
        assertEquals(result.getE(), baseData.getE());
        assertEquals(result.getF(), baseData.getF());
        assertEquals(result.getG(), baseData.getG(), 0.01);
        assertEquals(result.getI(), baseData.getI());
        assertEquals(result.getH(), baseData.getH(), 0.01);
        for (int i = 0; i < result.getJ().length; i++)
        {
            assertEquals(result.getJ()[i], baseData.getJ()[i]);
        }
        for (int i = 0; i < result.getK().length; i++)
        {
            assertEquals(result.getK()[i], baseData.getK()[i]);
        }
        for (int i = 0; i < result.getL().length; i++)
        {
            assertEquals(result.getL()[i], baseData.getL()[i]);
        }
        for (int i = 0; i < result.getM().length; i++)
        {
            assertEquals(result.getM()[i], baseData.getM()[i]);
        }
        for (int i = 0; i < result.getN().length; i++)
        {
            assertEquals(result.getN()[i], baseData.getN()[i]);
        }
        for (int i = 0; i < result.getO().length; i++)
        {
            assertEquals(result.getO()[i], baseData.getO()[i]);
        }
        for (int i = 0; i < result.getP().length; i++)
        {
            assertEquals(result.getP()[i], baseData.getP()[i], 0.1);
        }
        for (int i = 0; i < result.getQ().length; i++)
        {
            assertEquals(result.getQ()[i], baseData.getQ()[i], 0.1);
        }
        for (int i = 0; i < result.getR().length; i++)
        {
            assertEquals(result.getR()[i], baseData.getR()[i]);
        }
        //
    }

    @Test
    public void wrapTest() throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException, ClassNotFoundException, InstantiationException
    {
        WrapData wrapData  = new WrapData();
        JfireSE  jfireSE   = JfireSE.config().refTracking().staticRegisterClass(WrapData.class).build();
        byte[]   serialize = jfireSE.serialize(wrapData);
        WrapData result    = (WrapData) jfireSE.deSerialize(serialize);
        assertEquals(result.getA(), wrapData.getA());
        assertEquals(result.getB(), wrapData.getB());
        assertEquals(result.getC(), wrapData.getC());
        assertEquals(result.getD(), wrapData.getD());
        assertEquals(result.getE(), wrapData.getE());
        assertEquals(result.getF(), wrapData.getF());
        assertEquals(result.getG(), wrapData.getG(), 0.01);
        assertEquals(result.getH(), wrapData.getH(), 0.01);
        assertEquals(result.getI(), wrapData.getI());
        for (int i = 0; i < result.getJ().length; i++)
        {
            assertEquals(result.getJ()[i], wrapData.getJ()[i]);
        }
        for (int i = 0; i < result.getK().length; i++)
        {
            assertEquals(result.getK()[i], wrapData.getK()[i]);
        }
        for (int i = 0; i < result.getL().length; i++)
        {
            assertEquals(result.getL()[i], wrapData.getL()[i]);
        }
        for (int i = 0; i < result.getM().length; i++)
        {
            assertEquals(result.getM()[i], wrapData.getM()[i]);
        }
        for (int i = 0; i < result.getN().length; i++)
        {
            assertEquals(result.getN()[i], wrapData.getN()[i]);
        }
        for (int i = 0; i < result.getO().length; i++)
        {
            assertEquals(result.getO()[i], wrapData.getO()[i]);
        }
        for (int i = 0; i < result.getP().length; i++)
        {
            assertEquals(result.getP()[i], wrapData.getP()[i], 0.1);
        }
        for (int i = 0; i < result.getQ().length; i++)
        {
            assertEquals(result.getQ()[i], wrapData.getQ()[i], 0.1);
        }
        for (int i = 0; i < result.getR().length; i++)
        {
            assertEquals(result.getR()[i], wrapData.getR()[i]);
        }
        for (int i = 0; i < wrapData.getList().size(); i++)
        {
            BaseData a = wrapData.getList().get(i);
            BaseData b = result.getList().get(i);
            Assert.assertTrue(a.equals(b));
        }
        for (int i = 0; i < wrapData.getMap().size(); i++)
        {
            Assert.assertTrue(wrapData.getMap().get(i).equals(result.getMap().get(i)));
        }
        for (int i = 0; i < wrapData.getW().length; i++)
        {
            for (int j = 0; j < wrapData.getW()[i].length; j++)
            {
                Assert.assertEquals(result.getW()[i][j], wrapData.getW()[i][j]);
            }
        }
    }

    @Test
    public void referenceTest()
    {
        org.example.festest.data.Person person  = new org.example.festest.data.Person("linbin", 25);
        org.example.festest.data.Person tPerson = new org.example.festest.data.Person("zhangshi[in", 30);
        person.setLeader(tPerson);
        tPerson.setLeader(person);
        JfireSE                         jfireSE   = JfireSE.config().refTracking().build();
        byte[]                          serialize = jfireSE.serialize(person);
        org.example.festest.data.Person result    = (org.example.festest.data.Person) jfireSE.deSerialize(serialize);
        assertEquals("zhangshi[in", result.getLeader().getName());
    }

    @Test
    public void objectTest() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        JfireSE  jfireSE    = JfireSE.config().refTracking().build();
        Calendar calendar   = Calendar.getInstance();
        byte[]   serialize  = jfireSE.serialize(calendar);
        Calendar reCalendar = (Calendar) jfireSE.deSerialize(serialize);
        Assert.assertTrue(reCalendar.equals(calendar));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void listTest() throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        ArrayList<BaseData> list = new ArrayList<BaseData>();
        for (int i = 0; i < 5; i++)
        {
            list.add(new BaseData(i));
        }
        JfireSE             jfireSE   = JfireSE.config().refTracking().build();
        byte[]              serialize = jfireSE.serialize(list);
        ArrayList<BaseData> result    = (ArrayList<BaseData>) jfireSE.deSerialize(serialize);
        Assert.assertTrue(list.equals(result));
    }

    @Test
    public void baseDataTest()
    {
        JfireSE  jfireSE   = JfireSE.config().refTracking().build();
        BaseData baseData  = new BaseData();
        byte[]   serialize = jfireSE.serialize(baseData);
        BaseData result    = (BaseData) jfireSE.deSerialize(serialize);
        assertTrue(result.equals(baseData));
    }

    @Test
    public void objectArrayTest()
    {
        Object[] array = new Object[4];
        array[0] = new org.example.festest.data.Person();
        array[1] = new BaseData();
        array[2] = new LongData();
        array[3] = new WrapData();
        JfireSE  jfireSE   = JfireSE.config().refTracking().build();
        byte[]   serialize = jfireSE.serialize(array);
        Object[] result    = (Object[]) jfireSE.deSerialize(serialize);
        Assert.assertTrue(((Person) result[0]).equals(array[0]));
    }

    @Test
    public void byteArrayTest()
    {
        byte[]  array     = new byte[]{1, 2, 5, 6, 8, 9};
        JfireSE jfireSE   = JfireSE.config().refTracking().build();
        byte[]  serialize = jfireSE.serialize(array);
        byte[]  result    = (byte[]) jfireSE.deSerialize(serialize);
        for (int i = 0; i < array.length; i++)
        {
            assertEquals(array[i], result[i]);
        }
    }

    @Test
    public void booleanArrayTest()
    {
        boolean[] array     = new boolean[]{true, false, false, true, true, true};
        JfireSE   jfireSE   = JfireSE.config().refTracking().build();
        byte[]    serialize = jfireSE.serialize(array);
        boolean[] result    = (boolean[]) jfireSE.deSerialize(serialize);
        for (int i = 0; i < array.length; i++)
        {
            assertEquals(array[i], result[i]);
        }
    }

    @Test
    public void arrayDataTest()
    {
        JfireSE jfireSE   = JfireSE.config().refTracking().build();
        byte[]  serialize = jfireSE.serialize(new ArrayData());
        jfireSE.deSerialize(serialize);
    }

    @Test
    public void objectArrTest()
    {
        Random random = new Random();
        byte[] key    = new byte[16];
        random.nextBytes(key);
        Object[] data      = new Object[]{Integer.valueOf(14), new BaseData[]{new BaseData(), new BaseData()}};
        JfireSE  jfireSE   = JfireSE.config().refTracking().build();
        byte[]   serialize = jfireSE.serialize(data);
        Object[] result    = (Object[]) jfireSE.deSerialize(serialize);
        assertEquals(14, result[0]);
        assertEquals(((BaseData[]) data[1])[0], ((BaseData[]) result[1])[0]);
        assertEquals(((BaseData[]) data[1])[1], ((BaseData[]) result[1])[1]);
    }

    /**
     * 不注册类型直接序列化时，数组实例多次被引用能够正确序列化
     */
    @Test
    public void arryaNotRegisterClassSeri()
    {
        ArrayRefenceHolder holder    = new ArrayRefenceHolder();
        JfireSE            jfireSE   = JfireSE.config().refTracking().build();
        byte[]             serialize = jfireSE.serialize(holder);
        ArrayRefenceHolder result    = (ArrayRefenceHolder) jfireSE.deSerialize(serialize);
        assertArrayEquals(new int[]{1, 2}, result.getA()[0]);
        assertArrayEquals(new int[]{3, 4}, result.getA()[1]);
        assertArrayEquals(new int[]{1, 2}, result.getB()[0]);
        assertArrayEquals(new int[]{3, 4}, result.getB()[1]);
    }

    @Test
    public void methodObjectTest() throws NoSuchMethodException
    {
        Method  methodObjectTest = this.getClass().getDeclaredMethod("methodObjectTest");
        JfireSE jfireSE          = JfireSE.config().refTracking().build();
        byte[]  serialize        = jfireSE.serialize(methodObjectTest);
        Method  method           = (Method) jfireSE.deSerialize(serialize);
        System.out.println(method.equals(methodObjectTest));
        assertEquals(methodObjectTest, method);
    }

    @Test
    public void basetest()
    {
        JfireSE jfireSE = JfireSE.config().refTracking().build();
        User    user    = new User();
        user.setAge(123);
        user.setName("aaa");
        org.example.festest.Home home = new org.example.festest.Home();
        home.setAddress("ssss");
        home.setUser(user);
        user.setHome(home);
        byte[] serialize = jfireSE.serialize(user);
        User   another   = (User) jfireSE.deSerialize(serialize);
        Assert.assertEquals(user.getAge(), another.getAge());
        Assert.assertEquals(user.getName(), another.getName());
        Home home1 = user.getHome();
        Assert.assertEquals(home.getAddress(), home1.getAddress());
    }

    @Test
    public void longtest()
    {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        Output output = null;
        output = new Output(1, 15096);
        kryo.writeClassAndObject(output, new LongData());
        byte[] bb = output.toBytes();
        System.out.println("LongData序列化：kryo基础数据长度：" + bb.length);
        JfireSE jfireSE   = JfireSE.config().refTracking().build();
        byte[]  serialize = jfireSE.serialize(new LongData());
        System.out.println("LongData序列化：jfirese基础数据长度：" + serialize.length);
        System.out.println("序列化长度减少" + (bb.length - serialize.length));
        output = new Output(1, 15096);
        kryo.writeClassAndObject(output, new BaseData(1));
        bb = output.toBytes();
        log.debug("basedata序列化：kryo基础数据长度：{}", bb.length);
        byte[] serialize1 = jfireSE.serialize(new BaseData(1));
        log.info("basedata序列化：jfirese基础数据长度：" + serialize1.length);
        log.info("序列化长度减少{}", (bb.length - serialize1.length));
    }

    @Test
    public void mapTest()
    {
        MapDemo demo = new MapDemo();
        demo.getMap().put(1, new BaseData());
        JfireSE jfireSE   = JfireSE.config().staticRegisterClass(MapDemo.class).refTracking().build();
        byte[]  serialize = jfireSE.serialize(demo);
        jfireSE.deSerialize(serialize);
    }

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
        JfireSE jfireSE = JfireSE.config().build();
        byte[]  bytes   = jfireSE.serialize(testData);
        Assert.assertEquals(testData, jfireSE.deSerialize(bytes));
        TestData read = (TestData) jfireSE.deSerialize(bytes);
        Assert.assertEquals("xx", read.getSms()[0].getC());
        Assert.assertNull(read.getSms()[1]);
    }

    @Test
    public void test3()
    {
        Fury               fury   = Fury.builder().requireClassRegistration(false).withRefTracking(true).build();
        org.example.Home   home   = new org.example.Home();
        org.example.Person person = new org.example.Person();
        home.setPerson(person);
        person.setHome(home);
        fury.serialize(home);
        JfireSE jfireSE = JfireSE.config().refTracking().build();
        jfireSE.serialize(home);
    }
}
