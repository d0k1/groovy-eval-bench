package com.focusit.template;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

import java.io.*;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.focusit.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.template.internal.CustomTemplate;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 1, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 20, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(8)
public class TemplateBenchmark
{
    private static final String CALCULATION_SCRIPT = "System.currentTimeMillis();";
    private Template scriptTemplate;
    private String template;
    private String parsedTemplateBody;
    private Script compiledTemplate;
    private GroovyShell shell;
    private CompilerConfiguration cfg;

    @Setup
    public void init() throws IOException, ClassNotFoundException
    {
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("GroovyTemplate.txt"))
        {
            template = IOUtils.toString(is, "UTF-8");
        }
        String template1 = "<%" + CALCULATION_SCRIPT + "%>" + template;

        scriptTemplate = new SimpleTemplateEngine().createTemplate(template1);

        cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass("com.focusit.template.TemplateBaseClass");
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);

        shell = new GroovyShell(cfg);

        parsedTemplateBody = new CustomTemplate().parse(new StringReader(template));
        compiledTemplate = shell.parse(parsedTemplateBody);
    }

    @Benchmark
    public void templateNoCompilation(Blackhole bh) throws IOException, ClassNotFoundException
    {
        Bindings bindings = new SimpleBindings();
        bindings.put("key", "test!!!!!");

        String result = scriptTemplate.make(bindings).toString();
        bh.consume(result);
    }

    @Benchmark
    public void templateNoCompilationCustomParser(Blackhole bh) throws IOException, ClassNotFoundException
    {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        Binding bindings = new Binding();

        bindings.setProperty("key", "test!!!!!");
        bindings.setProperty("out", printWriter);

        compiledTemplate.setBinding(bindings);

        bh.consume(compiledTemplate.run());
        printWriter.flush();

        bh.consume(writer);
    }
}
