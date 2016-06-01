package com.focusit.groovy.groovyscript.groovyshell;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Groovy API example
 * Created by doki on 30.05.16.
 */
public class GroovyShellExample {

    public static void main(String args[]) throws IOException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScript.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(simpleScriptBody);
        script.setProperty("key", 123L);
        System.out.println(script.run());
    }

}
