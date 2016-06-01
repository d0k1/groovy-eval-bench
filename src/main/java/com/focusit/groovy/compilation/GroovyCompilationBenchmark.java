package com.focusit.groovy.compilation;

import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by doki on 01.06.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 20, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
@Threads(8)
public class GroovyCompilationBenchmark {
    private CompilerConfiguration cfg;
    private Map options;
    GroovyClassLoader loader;
    Class scriptClass;

    @Setup
    public void init() throws IOException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("GroovyScriptDerived.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        cfg = new CompilerConfiguration();
        options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setTargetBytecode("52");
        cfg.setTargetDirectory("/tmp/groovyc");
        cfg.setOptimizationOptions(options);
        GroovyClassLoader loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader(), cfg);
        scriptClass = loader.parseClass(simpleScriptBody, "GroovyScriptDerived");
    }

    @Benchmark
    public void scriptFromClassloader(Blackhole bh) throws IllegalAccessException, InstantiationException {
        Script script = (Script) scriptClass.newInstance();
        script.setProperty("key", 34234L);
        bh.consume(script.run());
    }
}