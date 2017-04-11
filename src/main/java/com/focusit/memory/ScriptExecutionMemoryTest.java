package com.focusit.memory;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.*;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.GroovyClassValue;
import org.codehaus.groovy.util.LazyReference;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Created by dkirpichenkov on 07.04.17.
 */
public class ScriptExecutionMemoryTest {
    private static GroovyClassValue<?> groovyGlobalClassValue;
    private static Field cachedClassRef;
    private static Field artifactClassLoader;
    private static Field strongMetaClass;

    private static Field fields;
    private static Field constructors;
    private static Field methods;
    private static Field cachedSuperClass;
    private static Field callSiteClassLoader;
    private static Field cachedClass;

    static
    {
        try
        {
            Field gcv = ClassInfo.class.getDeclaredField("globalClassValue");
            gcv.setAccessible(true);
            groovyGlobalClassValue = (GroovyClassValue<?>)gcv.get(null);

            cachedClassRef = ClassInfo.class.getDeclaredField("cachedClassRef");
            cachedClassRef.setAccessible(true);

            artifactClassLoader = ClassInfo.class.getDeclaredField("artifactClassLoader");
            artifactClassLoader.setAccessible(true);

            strongMetaClass = ClassInfo.class.getDeclaredField("strongMetaClass");
            strongMetaClass.setAccessible(true);

            fields = CachedClass.class.getDeclaredField("fields");
            fields.setAccessible(true);

            constructors = CachedClass.class.getDeclaredField("constructors");
            constructors.setAccessible(true);

            methods = CachedClass.class.getDeclaredField("methods");
            methods.setAccessible(true);

            cachedSuperClass = CachedClass.class.getDeclaredField("cachedSuperClass");
            cachedSuperClass.setAccessible(true);

            callSiteClassLoader = CachedClass.class.getDeclaredField("callSiteClassLoader");
            callSiteClassLoader.setAccessible(true);

            cachedClass = CachedClass.class.getDeclaredField("cachedClass");
            cachedClass.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace(System.err);
        }
    }

    private static void clearCachedClass(CachedClass cls) throws IllegalAccessException {
        ((LazyReference)fields.get(cls)).clear();
        ((LazyReference)constructors.get(cls)).clear();
        ((LazyReference)methods.get(cls)).clear();
        ((LazyReference)cachedSuperClass.get(cls)).clear();
        ((LazyReference)callSiteClassLoader.get(cls)).clear();
        cachedClass.set(cls, null);
        cls.classInfo = null;
    }

    private static void clearClassInfo(ClassInfo classInfo) throws IllegalAccessException {
        classInfo.setStrongMetaClass(null);
        ((LazyReference)cachedClassRef.get(classInfo)).clear();
        ((LazyReference)artifactClassLoader.get(classInfo)).clear();
    }

    private static void removeGroovyClass(final Class<?> scriptClass, final MetaClassRegistry metaClassRegistry)
    {
        metaClassRegistry.removeMetaClass(scriptClass);

        try {
            clearClassInfo(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getClassInfo());
            clearCachedClass(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getTheCachedClass());

        } catch (IllegalAccessException e) {
            e.printStackTrace(System.err);
        }

        groovyGlobalClassValue.remove(scriptClass);
        Introspector.flushFromCaches(scriptClass);
    }

    private static void removeGroovyClasses(final Class<?> scriptClass, boolean closeClassloader)
    {
        System.out.println(String.format("Removing script class %s from caches", scriptClass));
        final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        final ClassLoader classLoader = scriptClass.getClassLoader();
        if (classLoader instanceof GroovyClassLoader)
        {
            if (closeClassloader)
            {
                try (final GroovyClassLoader groovyClassLoader = (GroovyClassLoader)classLoader)
                {
                    for (Class<?> clazz : groovyClassLoader.getLoadedClasses())
                    {
                        removeGroovyClass(clazz, metaClassRegistry);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }
            else
            {
                removeGroovyClass(scriptClass, metaClassRegistry);
            }
        }
        else
        {
            System.out.println(String.format("Somehow %s is not a GroovyClassloader", classLoader.toString()));
            removeGroovyClass(scriptClass, metaClassRegistry);
        }
    }

    public static Class test()  throws IOException {
        System.out.println("Start");
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream("SimpleCallSite.groovy"))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(simpleScriptBody);

        script.setProperty("key", 123L);

        Object result = script.run();
        System.out.println("Result: "+result.getClass()+". "+result);

        System.in.read();

        return script.getClass();
    }

    public static void main(String[] args) throws IOException {
        Class klazz = test();

        removeGroovyClasses(klazz, true);

        System.in.read();

        klazz = test();

        removeGroovyClasses(klazz, true);

        System.in.read();
    }
}
