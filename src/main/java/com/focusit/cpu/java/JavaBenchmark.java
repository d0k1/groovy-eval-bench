package com.focusit.cpu.java;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Created by doki on 02.06.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 20, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
@Threads(8)
public class JavaBenchmark
{
    @Benchmark
    public void vanillaJava(Blackhole bh)
    {
        JavaDerivedClass derived = new JavaDerivedClass();
        derived.setKey(123L);
        bh.consume(derived.execute());
    }
}
