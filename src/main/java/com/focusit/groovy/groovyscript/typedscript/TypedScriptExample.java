package com.focusit.groovy.groovyscript.typedscript;

import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by doki on 30.05.16.
 */
public class TypedScriptExample {
    public static void main(String args[])
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String typedScriptBody;

        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("TypedScript.groovy")) {
            typedScriptBody = IOUtils.toString(is, "UTF-8");
        }
        CompilerConfiguration cfg = new CompilerConfiguration();
        Map options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setOptimizationOptions(options);
        cfg.setTargetBytecode("52");
        //In case you want to save compiled class for further analyze
        cfg.setTargetDirectory("/tmp/groovyc");
        GroovyShell shell = new GroovyShell(cfg);
        shell.parse(typedScriptBody);

        ITypedScript typedScript = (ITypedScript) shell.getClassLoader().loadClass("TypedScript").newInstance();

        System.out.println(typedScript.test(123L));
    }

}
