package com.focusit.groovy.expando;

import com.focusit.groovy.compilation.GroovyCompilationExample;
import com.focusit.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Created by doki on 04.08.16.
 */
public class ExpandoExample {
    public static void main(String args[]) throws IOException, IllegalAccessException, InstantiationException {
        String wrapperScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader()
                .getResourceAsStream("wrapper.groovy")) {
            wrapperScriptBody = IOUtils.toString(is, "UTF-8");
        }

        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleScriptWithMap.groovy")) {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        Binding ssbBinding = new Binding();
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        ssbBinding.setProperty("key", 123);
        ssbBinding.setProperty("map", map);

        GroovyClassLoader loader = new GroovyClassLoader(GroovyCompilationExample.class.getClassLoader());
        Class ssbKlazz = loader.parseClass(simpleScriptBody);
        Script ssb = (Script) ssbKlazz.newInstance();
        ssb.setBinding(ssbBinding);


        Class wsbKlazz = loader.parseClass(wrapperScriptBody);
        Script wsb = (Script) wsbKlazz.newInstance();
        Binding wsbBinding = new Binding();
        wsbBinding.setProperty("script", ssb);
        wsb.setBinding(wsbBinding);
        System.out.println("result: " + wsb.run().toString());
    }
}
