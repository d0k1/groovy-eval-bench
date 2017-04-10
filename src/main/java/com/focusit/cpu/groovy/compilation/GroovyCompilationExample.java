package com.focusit.cpu.groovy.compilation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

/**
 * Created by doki on 01.06.16.
 */
public class GroovyCompilationExample
{
    public static void main(String args[])
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("GroovyScriptDerived.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        CompilerConfiguration cfg = new CompilerConfiguration();
        Map options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setTargetBytecode("52");
        //In case you want to save compiled class for further analyze
        //cfg.setTargetDirectory("/tmp/groovyc");
        cfg.setOptimizationOptions(options);

        GroovyClassLoader loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader(), cfg);
        Class scriptClass = loader.parseClass(simpleScriptBody, "GroovyScriptDerived");
        Script script = (Script)scriptClass.newInstance();
        script.setProperty("key", 34234L);
        System.out.println(script.run());
    }
}
