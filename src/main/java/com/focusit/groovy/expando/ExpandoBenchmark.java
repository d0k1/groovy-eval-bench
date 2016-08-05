package com.focusit.groovy.expando;

import com.focusit.groovy.compilation.GroovyCompilationExample;
import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by doki on 05.08.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 5, time = 30, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 30, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
@Threads(50)
public class ExpandoBenchmark {

    private Class cachedSsbKlazz;
    private Class cachedWsbKlazz;
    private Class cachedWsbKlazz2;
    private Class cachedWsbKlazz3;

    private String wrapperScriptBody;
    private String wrapperScriptBody2;
    private String wrapperScriptBody3;
    private String simpleScriptBody;
    private GroovyClassLoader cachedLoader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader());

    @Setup
    public void init() throws IOException {
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("wrapper.groovy")) {
            wrapperScriptBody = IOUtils.toString(is, "UTF-8");
        }

        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("wrapper_with_static.groovy")) {
            wrapperScriptBody2 = IOUtils.toString(is, "UTF-8");
        }

        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("wrapper_no_expando.groovy")) {
            wrapperScriptBody3 = IOUtils.toString(is, "UTF-8");
        }

        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScriptWithMap.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        cachedLoader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader());
        cachedSsbKlazz = cachedLoader.parseClass(simpleScriptBody);
        cachedWsbKlazz = cachedLoader.parseClass(wrapperScriptBody);
        cachedWsbKlazz2 = cachedLoader.parseClass(wrapperScriptBody2);
        cachedWsbKlazz3 = cachedLoader.parseClass(wrapperScriptBody3);
    }

    @Benchmark
    public void groovyExpandoBenchmarkFullCompile(Blackhole bh) throws IllegalAccessException, InstantiationException {
        Binding ssbBinding = new Binding();
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        ssbBinding.setProperty("key", 123);
        ssbBinding.setProperty("map", map);

        GroovyClassLoader loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader());
        Class ssbKlazz = loader.parseClass(simpleScriptBody);
        Script ssb = (Script) ssbKlazz.newInstance();
        ssb.setBinding(ssbBinding);

        Class wsbKlazz = loader.parseClass(wrapperScriptBody);
        Script wsb = (Script) wsbKlazz.newInstance();
        Binding wsbBinding = new Binding();
        wsbBinding.setProperty("script", ssb);
        wsb.setBinding(wsbBinding);

        bh.consume(wsb.run().toString());
    }

    @Benchmark
    public void groovyExpandoBenchmarkCachedNoCompile(Blackhole bh) throws IllegalAccessException, InstantiationException {
        Binding ssbBinding = new Binding();
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        ssbBinding.setProperty("key", 123);
        ssbBinding.setProperty("map", map);
        Script ssb = (Script) cachedSsbKlazz.newInstance();
        ssb.setBinding(ssbBinding);

        Script wsb = (Script) cachedWsbKlazz.newInstance();
        Binding wsbBinding = new Binding();
        wsbBinding.setProperty("script", ssb);
        wsb.setBinding(wsbBinding);

        bh.consume(wsb.run().toString());
    }

    @Benchmark
    public void groovyExpandoBenchmarkCachedNoCompileWithStatic(Blackhole bh) throws IllegalAccessException, InstantiationException {
        Binding ssbBinding = new Binding();
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        ssbBinding.setProperty("key", 123);
        ssbBinding.setProperty("map", map);
        Script ssb = (Script) cachedSsbKlazz.newInstance();
        ssb.setBinding(ssbBinding);

        Script wsb = (Script) cachedWsbKlazz2.newInstance();
        Binding wsbBinding = new Binding();
        wsbBinding.setProperty("script", ssb);
        wsb.setBinding(wsbBinding);

        bh.consume(wsb.run().toString());
    }

    @Benchmark
    public void groovyExpandoBenchmarkCachedNoCompileNoExpando(Blackhole bh) throws IllegalAccessException, InstantiationException {
        Binding ssbBinding = new Binding();
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        ssbBinding.setProperty("key", 123);
        ssbBinding.setProperty("map", map);
        Script ssb = (Script) cachedSsbKlazz.newInstance();
        ssb.setBinding(ssbBinding);

        Script wsb = (Script) cachedWsbKlazz3.newInstance();
        Binding wsbBinding = new Binding();
        wsbBinding.setProperty("script", ssb);
        wsb.setBinding(wsbBinding);

        bh.consume(wsb.run().toString());
    }
}
