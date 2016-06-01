package com.focusit.groovy.groovyscript.staticcompilation;

import com.focusit.groovy.compilation.GroovyCompilationExample;
import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.groovy.groovyscript.scriptinheritance.GroovyScriptBase;
import groovy.lang.GroovyClassLoader;
import groovy.transform.CompileStatic;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
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
public class StaticCompilationBenchmark {
    private CompilerConfiguration cfg;
    private Map options;
    private GroovyClassLoader loader;
    private Class scriptClass;

    @Setup
    public void init() throws IOException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("StaticCompilableScript.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        cfg = new CompilerConfiguration();
        options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setOptimizationOptions(options);
        cfg.setTargetBytecode("52");
        cfg.setScriptBaseClass("com.focusit.groovyscript.scriptinheritance.GroovyScriptBase");
        cfg.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));

        loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader(), cfg);
        scriptClass = loader.parseClass(simpleScriptBody, "StaticCompilableScript");
    }

    @Benchmark
    public void testStaticCompiledScript(Blackhole bh) throws IllegalAccessException, InstantiationException {
        GroovyScriptBase script = (GroovyScriptBase) scriptClass.newInstance();
        script.setKey(1234L);
        bh.consume(script.run());
    }
}
