package com.focusit.groovyscript.jsr223;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.script.*;

import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.focusit.groovyscript.groovyshell.GroovyShellExample;

/**
 * Created by doki on 30.05.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(4)
@Threads(8)
public class GroovyJSR223Benchmark
{
    private ScriptEngineManager engineManager;
    private String simpleScriptBody;
    private ScriptEngine engine;
    private CompiledScript script;
    private ScriptContext ctx;

    @Setup
    public void init() throws IOException, ScriptException
    {
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScript.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("groovy");
        script = ((Compilable)(engine)).compile(simpleScriptBody);

        Bindings bindings = new SimpleBindings();
        bindings.put("key", 123L);
        ctx = engine.getContext();
        ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    @Benchmark
    public void groovyNoEngineInOneCycle(Blackhole bh) throws ScriptException
    {
        CompiledScript script = ((Compilable)(engine)).compile(simpleScriptBody);

        Bindings bindings = new SimpleBindings();
        bindings.put("key", 123L);

        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        bh.consume(script.eval(ctx));
    }

    @Benchmark
    public void groovyNoEngineNoScriptInOneCycle(Blackhole bh) throws ScriptException
    {
        Bindings bindings = new SimpleBindings();
        bindings.put("key", 123L);

        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        bh.consume(script.eval(ctx));
    }

    @Benchmark
    //TODO It could be a nice benchmark but it doesn't work at all. Donno why but groovy script can't get its bindings
    public void groovyNoEngineNoScriptLessBindingInOneCycle(Blackhole bh) throws ScriptException
    {
        Object result = script.eval(ctx);
        bh.consume(result);
    }
}