package com.focusit.cpu.groovy.template;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.focusit.cpu.groovy.POJOBinding;
import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.cpu.groovy.template.internal.CustomTemplate;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public class CustomTemplateExample
{
    public static final String CALCULATION_SCRIPT = "System.currentTimeMillis();";

    public static String dynamicScript(Script compiledTemplate)
    {
        Binding bindings = new Binding();
        bindings.setProperty("key", "test!!!!!");
        StringWriter writer = new StringWriter();
        bindings.setProperty("out", writer);
        compiledTemplate.setBinding(bindings);
        compiledTemplate.run();
        String result = writer.toString();
        return result;
    }

    public static String staticScript(Class<TemplateBaseClass> templateClass, POJOBinding binding)
            throws IllegalAccessException, InstantiationException, IOException
    {
        TemplateBaseClass base = templateClass.newInstance();
        base.setKey("123123");

        StringWriter writer = new StringWriter();
        base.setOut(writer);
        base.setBinding(binding);
        base.execute();
        return writer.toString();
    }

    public static void main(String args[])
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        String template;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("GroovyTemplate.txt"))
        {
            template = IOUtils.toString(is, "UTF-8");
        }

        String template1 = "<%" + CALCULATION_SCRIPT + "%>" + template;
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass(TemplateBaseClass.class.getName());
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);

        GroovyShell shell = new GroovyShell(cfg);

        String parsedTemplateBody = new CustomTemplate().parse(new StringReader(template1));
        shell.parse(parsedTemplateBody, "TemplateForTest.groovy");
        Class<TemplateBaseClass> cls = (Class<TemplateBaseClass>)shell.getClassLoader().loadClass("TemplateForTest");
        POJOBinding binding = new POJOBinding();

        //        for (int i = 0; i < 50000000; i++) {
        //            staticScript(cls, binding);
        //            dynamicScript(compiledTemplate);
        //        }
        //dynamicScript(compiledTemplate);
        System.out.print(staticScript(cls, binding));
    }

}
