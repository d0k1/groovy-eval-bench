package com.focusit.cpu.groovy.groovyscript.typedscript;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;

import groovy.lang.GroovyShell;

/**
 * Created by doki on 31.05.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 20, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
@Threads(8)
public class TypedScriptBenchmark
{

    private String typedScriptBody;
    private GroovyShell shell;
    private CompilerConfiguration cfg;
    ITypedScript typedScript;
    Class typedClass;

    @Setup
    public void init()
            throws IOException, ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("TypedScript.groovy"))
        {
            typedScriptBody = IOUtils.toString(is, "UTF-8");
        }

        cfg = new CompilerConfiguration();
        Map options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setOptimizationOptions(options);
        cfg.setTargetBytecode("52");

        shell = new GroovyShell(cfg);
        shell.parse(typedScriptBody);

        typedClass = shell.getClassLoader().loadClass("TypedScript");
        typedScript = (ITypedScript)typedClass.newInstance();
    }

    @Benchmark
    public void groovyNoShellNoCompile(Blackhole bh)
            throws ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {

        ITypedScript typedScript = (ITypedScript)typedClass.newInstance();

        bh.consume(typedScript.test(123L));
    }

    @Benchmark
    public void groovyNoShellNoCompileNoInstantiate(Blackhole bh)
            throws ScriptException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        bh.consume(typedScript.test(123L));
    }
}
