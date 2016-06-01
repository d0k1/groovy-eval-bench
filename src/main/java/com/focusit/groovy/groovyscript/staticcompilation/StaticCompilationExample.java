package com.focusit.groovy.groovyscript.staticcompilation;

import com.focusit.groovy.compilation.GroovyCompilationExample;
import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import com.focusit.groovy.groovyscript.scriptinheritance.GroovyScriptBase;
import groovy.lang.GroovyClassLoader;
import groovy.transform.CompileStatic;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by doki on 01.06.16.
 */
public class StaticCompilationExample {
    public static void main(String args[])
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("StaticCompilableScript.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        CompilerConfiguration cfg;
        Map options;
        GroovyClassLoader loader;
        Class scriptClass;

        cfg = new CompilerConfiguration();
        options = new HashMap<>();
        options.put(CompilerConfiguration.INVOKEDYNAMIC, true);
        cfg.setOptimizationOptions(options);
        cfg.setTargetBytecode("52");
        //In case you want to save compiled class for further analyze
        //cfg.setTargetDirectory("/tmp/groovyc");
        cfg.setScriptBaseClass("com.focusit.groovyscript.scriptinheritance.GroovyScriptBase");
        cfg.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic.class));

        loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader(), cfg);
        scriptClass = loader.parseClass(simpleScriptBody, "StaticCompilableScript");
        GroovyScriptBase script = (GroovyScriptBase) scriptClass.newInstance();
        script.setKey(1234L);
        System.out.println(script.run());
    }
}
