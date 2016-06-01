package com.focusit.groovyscript.scriptinheritance;

/**
 * Created by doki on 31.05.16.
 */

import com.focusit.POJOBinding;
import com.focusit.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@Threads(8)
public class ScriptInheritanceBenchmark {
    private String simpleScriptBody;
    private GroovyShell shell;
    private Class<GroovyScriptBase> baseClass;

    @Setup
    public void init() throws IOException, ClassNotFoundException {
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("GroovyScriptDerived.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);
        cfg.setScriptBaseClass("com.focusit.groovyscript.scriptinheritance.GroovyScriptBase");

        shell = new GroovyShell(cfg);
        shell.parse(simpleScriptBody, "GroovyScriptDerived.groovy");

        baseClass = (Class<GroovyScriptBase>) shell.getClassLoader().loadClass("GroovyScriptDerived");
    }

    @Benchmark
    public void groovyNoCompileCustomBinding(Blackhole bh) throws IllegalAccessException, InstantiationException {
        GroovyScriptBase base = baseClass.newInstance();
        base.setKey(123L);
        base.setBinding(new POJOBinding());

        bh.consume(base.execute());
    }
}
