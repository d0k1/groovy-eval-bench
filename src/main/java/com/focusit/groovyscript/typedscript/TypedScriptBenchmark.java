package com.focusit.groovyscript.typedscript;

import com.focusit.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by doki on 31.05.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@Threads(8)
public class TypedScriptBenchmark {

    private String typedScriptBody;
    private GroovyShell shell;
    ITypedScript typedScript;

    @Setup
    public void init() throws IOException, ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("TypedScript.groovy")) {
            typedScriptBody = IOUtils.toString(is, "UTF-8");
        }
        shell = new GroovyShell();
        typedScript = (ITypedScript) shell.getClassLoader().loadClass("TypedScript").newInstance();
    }

    @Benchmark
    public void groovyNoShellNoCompile(Blackhole bh) throws ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        ITypedScript typedScript = (ITypedScript) shell.getClassLoader().loadClass("TypedScript").newInstance();
        bh.consume(typedScript.test(123L));
    }

    @Benchmark
    public void groovyNoShellNoCompileNoInstantiate(Blackhole bh) throws ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        bh.consume(typedScript.test(123L));
    }
}
