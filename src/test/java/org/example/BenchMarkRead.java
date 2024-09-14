package org.example;

import com.jfirer.se2.JfireSE;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.example.sm.TestDataSm;
import org.example.sm2.TestDataSm2;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 3, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchMarkRead
{
    Fury     fury       = Fury.builder().withLanguage(Language.JAVA)//
                              .requireClassRegistration(false)//
                              .withRefTracking(true).build();
    JfireSE  jfireSE    = JfireSE.config().refTracking().build();
    TestData data       = new TestData().setTestDataSm(new TestDataSm()).setTestDataSm2(new TestDataSm2());
    byte[]   serialize  = jfireSE.serialize(data);
    byte[]   serialize2 = fury.serialize(data);

    @Benchmark
    public void testJfireSERead()
    {
        jfireSE.deSerialize(serialize);
    }

    @Benchmark
    public void testFuryRead()
    {
        fury.deserialize(serialize2);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(BenchMarkRead.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
