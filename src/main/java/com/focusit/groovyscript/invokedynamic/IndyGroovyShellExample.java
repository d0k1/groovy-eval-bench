package com.focusit.groovyscript.invokedynamic;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Groovy API example
 * Created by doki on 30.05.16.
 */
public class IndyGroovyShellExample
{

    public static void main(String args[]) throws IOException
    {
        String simpleScriptBody;
        try (InputStream is = IndyGroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScript.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        CompilerConfiguration cfg = new CompilerConfiguration();
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);

        GroovyShell shell = new GroovyShell(cfg);
        Script script = shell.parse(simpleScriptBody);
        script.setProperty("key", 123L);
        System.out.println(script.run());
    }

}
