package org.example.festest;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.jfirer.se2.JfireSE;
import lombok.extern.slf4j.Slf4j;
import org.example.festest.data.BaseData;
import org.example.festest.data.LongData;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

@Slf4j
public class LongTest
{
    @Test
    public void longtest() throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException, ClassNotFoundException, InstantiationException
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
}
