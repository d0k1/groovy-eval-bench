package com.focusit.groovy.groovyscript.scriptinheritance;

import com.focusit.groovy.POJOBinding;
import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.InputStream;

import static org.codehaus.groovy.control.CompilerConfiguration.INVOKEDYNAMIC;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public class ScriptInheritanceExample {
    public static void main(String args[])
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("GroovyScriptDerived.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        CompilerConfiguration cfg = new CompilerConfiguration();
        //In case you want to save compiled class for further analyze
        //cfg.setTargetDirectory("/tmp/groovyc");
        cfg.getOptimizationOptions().put(INVOKEDYNAMIC, true);
        cfg.setScriptBaseClass("com.focusit.groovyscript.scriptinheritance.GroovyScriptBase");

        GroovyShell shell = new GroovyShell(cfg);
        shell.parse(simpleScriptBody, "GroovyScriptDerived.groovy");

        GroovyScriptBase base = (GroovyScriptBase) shell.getClassLoader().loadClass("GroovyScriptDerived").newInstance();
        base.setBinding(new POJOBinding());
        base.setKey(123);
        System.out.println(base.execute());
    }
}
