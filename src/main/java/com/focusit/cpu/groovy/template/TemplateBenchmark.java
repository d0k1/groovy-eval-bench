package com.focusit.cpu.groovy.template;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.focusit.cpu.groovy.POJOBinding;
import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.cpu.groovy.template.internal.CustomTemplate;

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
@Warmup(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS) //4
@Measurement(iterations = 10, time = 20, timeUnit = TimeUnit.SECONDS) //8
@Fork(4)
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
    private Class templateClass;

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
        cfg.setScriptBaseClass(TemplateBaseClass.class.getName());
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);

        shell = new GroovyShell(cfg);

        parsedTemplateBody = new CustomTemplate().parse(new StringReader(template1));
        compiledTemplate = shell.parse(parsedTemplateBody, "CustomTemplate.groovy");
        templateClass = shell.getClassLoader().loadClass("CustomTemplate");
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
        Binding bindings = new Binding();

        StringWriter writer = new StringWriter();

        bindings.setVariable("key", "test!!!!!");
        bindings.setVariable("out", writer);

        compiledTemplate.setBinding(bindings);
        Object result = compiledTemplate.run();
        bh.consume(result);
        bh.consume(writer);
    }

    @Benchmark
    public void templateNoCompilationCustomParserTyped(Blackhole bh)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        StringWriter writer = new StringWriter();

        TemplateBaseClass base = (TemplateBaseClass)templateClass.newInstance();
        base.setKey("test???");
        base.setOut(writer);
        base.setBinding(new POJOBinding());
        Object result = base.execute();
        bh.consume(result);
        bh.consume(writer);
    }
}
