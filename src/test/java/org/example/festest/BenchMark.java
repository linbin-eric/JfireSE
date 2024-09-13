package org.example.festest;

import com.jfirer.fse.ByteArray;
import com.jfirer.se2.JfireSE;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 3)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchMark
{
    JfireSE   jfireSE = JfireSE.supportRefTracking(true).build();
    TestData  data    = new TestData();
    ByteArray buf     = ByteArray.allocate(100);
    Fury      fury    = Fury.builder().withLanguage(Language.JAVA)//
                            .requireClassRegistration(false)//
                            .withRefTracking(true).build();

    @Benchmark
    public void testFury()
    {
        byte[] bytes = fury.serialize(data);
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
