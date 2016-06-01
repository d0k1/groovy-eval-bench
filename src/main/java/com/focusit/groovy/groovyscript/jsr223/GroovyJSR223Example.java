package com.focusit.groovy.groovyscript.jsr223;

import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import org.apache.commons.io.IOUtils;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Example of JSR223 with groovy
 * Created by doki on 30.05.16.
 */
public class GroovyJSR223Example {
    public static void main(String args[]) throws ScriptException, IOException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScript.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("groovy");
        CompiledScript script = ((Compilable) (engine)).compile(simpleScriptBody);

        Bindings bindings = new SimpleBindings();
        bindings.put("key", 123L);

        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        System.out.println(script.eval(ctx));
    }
}
