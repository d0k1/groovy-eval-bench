package com.focusit.memory;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dkirpichenkov on 27.04.17.
 */
public class SoftLinkTrouble {
    private static Class wrapper;
    private static ExecutorService executor = Executors.newFixedThreadPool(150, r -> {
        Thread th = new Thread(r);
        th.setDaemon(true);
        return th;
    });

    public static Object[] execScript(String scriptName, int times) throws IOException, IllegalAccessException, InstantiationException {
        final String[] simpleScriptBody = new String[1];
        Object results[] = new Object[times];

        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream(scriptName))
        {
            simpleScriptBody[0] = IOUtils.toString(is, "UTF-8");
        }

        for(int i=0;i<times;i++) {
            int finalI = i;
            executor.execute(()->{
                try {
                simpleScriptBody[0] += ' ';
                GroovyClassLoader loader = new GroovyClassLoader(ClassLoader.getSystemClassLoader());
                Class scriptClass = loader.parseClass(simpleScriptBody[0], "Compiled"+System.currentTimeMillis());
                Script script = null;
                    script = (Script) scriptClass.newInstance();
                    script.setProperty("key", 123L);

                    Script ws = (Script) wrapper.newInstance();
                    ws.setProperty("wrapped", script);
                    results[finalI] = ws.run();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return results;
    }

    // -Xmx2g -Xms2g -XX:-UseCompressedClassPointers -XX:MaxMetaspaceSize=64m
    public static void main(String[] args) throws IllegalAccessException, IOException, InstantiationException {

        compileWrapper();

//        while(1==1) {
            execScript("SimpleCallSite.groovy", 1);
            System.out.println("5000 Done");
            System.in.read();
//        }
    }

    private static void compileWrapper() throws IOException {
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("GroovyStaticInit.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        wrapper = new GroovyClassLoader(ClassLoader.getSystemClassLoader()).parseClass(simpleScriptBody);
    }
}
