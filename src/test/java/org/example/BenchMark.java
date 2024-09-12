package org.example;

import com.jfirer.fse.ByteArray;
import com.jfirer.fse.Fse;
import com.jfirer.se2.JfireSE;
import io.fury.Fury;
import io.fury.config.Language;
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
public class BenchMark
{
    Fse       fse     = new Fse();
    Fse       fse_3   = new Fse().useCompile();
    TestData  data    = new TestData().setTestDataSm(new TestDataSm()).setTestDataSm2(new TestDataSm2());
    ByteArray buf     = ByteArray.allocate(100);
    Fury      fury    = Fury.builder().withLanguage(Language.JAVA)//
                            .requireClassRegistration(false)//
                            .withRefTracking(true).build();
    JfireSE   jfireSE = JfireSE.supportRefTracking(true).build();

    @Benchmark
    public void testFSENoCompile()
    {
        buf.clear();
        fse.serialize(data, buf);
    }

    @Benchmark
    public void testFury()
    {
        byte[] bytes = fury.serialize(data);
    }

    @Benchmark
    public void testFSEDirectCompile()
    {
        buf.clear();
        fse_3.serialize(data, buf);
    }

    @Benchmark
    public void testJfireSE()
    {
        jfireSE.serialize(data);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(BenchMark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
