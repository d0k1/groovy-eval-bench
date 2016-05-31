package com.focusit.groovyscript.typedscript;

import com.focusit.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by doki on 30.05.16.
 */
public class TypedScriptExample {
    public static void main(String args[]) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String typedScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("TypedScript.groovy")) {
            typedScriptBody = IOUtils.toString(is, "UTF-8");
        }
        GroovyShell shell = new GroovyShell();
        shell.parse(typedScriptBody);

        ITypedScript typedScript = (ITypedScript) shell.getClassLoader().loadClass("TypedScript").newInstance();

        System.out.println(typedScript.test(123L));
    }

}
