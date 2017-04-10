package com.focusit.memory;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClassImpl;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.reflection.ClassInfo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dkirpichenkov on 07.04.17.
 */
public class ScriptExecutionMemoryTest {
    public static int testF(){
        return (int) (Math.random()*100);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Start");
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleCallSite.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(simpleScriptBody);

        script.setProperty("key", 123L);

        ClassInfo.getClassInfo(script.getMetaClass().getTheClass()).getCachedClass().getCallSiteLoader()

        Object result = script.run();
        System.out.println("Result: "+result.getClass()+". "+result);

        System.out.println(String.format("Test %d, %d", testF(), testF()));

        System.in.read();
    }
}
