package org.example;

import com.jfirer.fse.ByteArray;
import com.jfirer.fse.Fse;
import com.jfirer.se.JfireSE;
import io.fury.Fury;
import io.fury.config.Language;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchMark
{
    Fse       fse     = new Fse();
    Fse       fse_3   = new Fse().useCompile();
    TestData  data    = new TestData();
    ByteArray buf     = ByteArray.allocate(100);
    Fury      fury    = Fury.builder().withLanguage(Language.JAVA)//
                            .requireClassRegistration(false)//
                            .withRefTracking(true).build();
    JfireSE   jfireSE = new JfireSE();

    @Benchmark
    public void testNoCompile()
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
    public void testDirectCompile()
    {
        buf.clear();
        fse_3.serialize(data, buf);
    }

    @Benchmark
    public void testJfireSE()
    {
        jfireSE.writeBytes(data);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(BenchMark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
