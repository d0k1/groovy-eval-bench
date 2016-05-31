package com.focusit.template;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

import java.io.*;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.focusit.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.template.internal.CustomTemplate;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public class CustomTemplateExample
{
    public static final String CALCULATION_SCRIPT = "System.currentTimeMillis();";

    public static void main(String args[]) throws IOException, ClassNotFoundException
    {
        String template;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("GroovyTemplate.txt"))
        {
            template = IOUtils.toString(is, "UTF-8");
        }

        String template1 = "<%" + CALCULATION_SCRIPT + "%>" + template;
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.setScriptBaseClass("com.focusit.template.TemplateBaseClass");
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);

        GroovyShell shell = new GroovyShell(cfg);

        String parsedTemplateBody = new CustomTemplate().parse(new StringReader(template1));
        Script compiledTemplate = shell.parse(parsedTemplateBody);
        Binding bindings = new Binding();
        bindings.setProperty("key", "test!!!!!");

        StringWriter writer = new StringWriter();
        bindings.setProperty("out", new PrintWriter(writer));

        compiledTemplate.setBinding(bindings);
        compiledTemplate.run();
        String result = writer.toString();

        System.out.println(result);
    }

}
