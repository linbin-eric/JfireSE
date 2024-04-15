package org.example;

import io.github.karlatemp.unsafeaccessor.Unsafe;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class BenchMarkUnsafe
{
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    char c   = '你';
    char Rec = Character.reverseBytes(c);
    private byte[] array  = new byte[2];
    private long   offset = UNSAFE.arrayBaseOffset(byte[].class);

    @Benchmark
    public void write()
    {
        UNSAFE.putChar(array, offset, c);
    }

    @Benchmark
    public void writeRe()
    {
        UNSAFE.putChar(array, offset, Rec);
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder().include(BenchMarkUnsafe.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
