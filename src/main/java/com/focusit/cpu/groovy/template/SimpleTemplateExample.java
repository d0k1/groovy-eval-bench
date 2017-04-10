package com.focusit.cpu.groovy.template;

import java.io.IOException;
import java.io.InputStream;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.commons.io.IOUtils;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public class SimpleTemplateExample
{
    public static final String CALCULATION_SCRIPT = "System.currentTimeMillis();";

    public static void main(String args[]) throws IOException, ClassNotFoundException
    {
        String template;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("GroovyTemplate.txt"))
        {
            template = IOUtils.toString(is, "UTF-8");
        }

        Bindings bindings = new SimpleBindings();
        bindings.put("key", "test!!!!!");
        String template1 = "<%" + CALCULATION_SCRIPT + "%>" + template;
        Template scriptTemplate = new SimpleTemplateEngine().createTemplate(template1);
        String result = scriptTemplate.make(bindings).toString();
        System.out.println(result);

        template1 = "<%" + CALCULATION_SCRIPT + "%>" + template;
        scriptTemplate = new SimpleTemplateEngine().createTemplate(template1);
        result = scriptTemplate.make(bindings).toString();
        System.out.println(result);
    }
}
