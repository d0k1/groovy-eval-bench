package com.focusit.memory;

import com.focusit.cpu.groovy.groovyscript.groovyshell.GroovyShellExample;
import groovy.lang.*;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.reflection.*;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.MetaClassSite;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.util.FastArray;
import org.codehaus.groovy.util.LazyReference;
import org.codehaus.groovy.util.ReferenceBundle;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

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
    private static Field reference;

    private static Field classes;
    private static Field metaClass;
    private static Field pogoCallSiteConstructor, pojoCallSiteConstructor, staticCallSiteConstructor;

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

            reference = LazyReference.class.getDeclaredField("reference");
            reference.setAccessible(true);

            classes = ClassLoader.class.getDeclaredField("classes");
            classes.setAccessible(true);

            metaClass = MetaClassSite.class.getDeclaredField("metaClass");
            metaClass.setAccessible(true);

            pogoCallSiteConstructor = CachedMethod.class.getDeclaredField("pogoCallSiteConstructor");
            pogoCallSiteConstructor.setAccessible(true);

            pojoCallSiteConstructor = CachedMethod.class.getDeclaredField("pojoCallSiteConstructor");
            pojoCallSiteConstructor.setAccessible(true);

            staticCallSiteConstructor = CachedMethod.class.getDeclaredField("staticCallSiteConstructor");
            staticCallSiteConstructor.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace(System.err);
        }
    }

    private static void clearCachedMethods(CachedMethod[] methods) throws IllegalAccessException {
        for(int i=0;i<methods.length;i++) {
            CachedMethod method = methods[i];
            clearCachedMethod(method);
//            methods[i] = null;
        }
    }

    private static void clearCachedMethod(CachedMethod method) throws IllegalAccessException {
        SoftReference ref = (SoftReference) pojoCallSiteConstructor.get(method);
        if (ref != null) {
            ref.clear();
            pojoCallSiteConstructor.set(method, null);
        }

        ref = (SoftReference) pogoCallSiteConstructor.get(method);
        if (ref != null) {
            ref.clear();
            pogoCallSiteConstructor.set(method, null);
        }

        ref = (SoftReference) staticCallSiteConstructor.get(method);
        if (ref != null) {
            ref.clear();
            staticCallSiteConstructor.set(method, null);
        }
    }

    private static void clearInstanceMethods(CachedClass cls) throws IllegalAccessException {
        MetaClassRegistryImpl mcri = (MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry();
        FastArray methods = mcri.getInstanceMethods();
        for(int i=0;i<methods.size();i++){
            if(methods.get(i) instanceof GeneratedMetaMethod.Proxy) {
                GeneratedMetaMethod m = (GeneratedMetaMethod) methods.get(i);
                clearCachedClass(m.getDeclaringClass());
            }
        }
    }

    private static void clearCachedClass(CachedClass cls) throws IllegalAccessException {
        ((LazyReference)fields.get(cls)).clear();

        if(methods.get(cls)!=null) {
            CachedMethod[] cachedMethods = (CachedMethod[]) ((LazyReference) methods.get(cls)).get();
            ((LazyReference) methods.get(cls)).clear();
            clearCachedMethods(cachedMethods);
        }

        ((LazyReference)constructors.get(cls)).clear();
        CachedConstructor[] cachedConstructors = (CachedConstructor[]) ((LazyReference)constructors.get(cls)).get();
        for(int i=0;i<cachedConstructors.length;i++){
            cachedConstructors[i] = null;
        }

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
        MetaClassImpl mci = (MetaClassImpl) metaClassRegistry.getMetaClass(scriptClass);
//        System.err.println(mci);
        try {
            clearClassInfo(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getClassInfo());

            clearInstanceMethods(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getTheCachedClass());
            clearCachedClass(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getTheCachedClass());

        } catch (IllegalAccessException e) {
            e.printStackTrace(System.err);
        }

        metaClassRegistry.removeMetaClass(scriptClass);

        groovyGlobalClassValue.remove(scriptClass);
        Introspector.flushFromCaches(scriptClass);
    }

    private static void removeCallSites(Class<?> scriptClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getCallSiteArray = scriptClass.getDeclaredMethod("$getCallSiteArray", null);
        getCallSiteArray.setAccessible(true);
        CallSite[] cs = (CallSite[]) getCallSiteArray.invoke(scriptClass, null);
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        for(int i=0;i<cs.length;i++){
            CallSite site = cs[i];
//            System.out.println("Class: "+site.getClass()+" Loader: "+site.getClass().getClassLoader());
            if(site instanceof MetaClassSite){
                MetaClassImpl mci = (MetaClassImpl) metaClass.get((MetaClassSite) site);
                removeGroovyClass(mci.getClassInfo().getTheClass(), metaClassRegistry);
            } else {
                try {
                    Field mc = site.getClass().getDeclaredField("metaClass");
                    mc.setAccessible(true);
                    MetaClassImpl mci = (MetaClassImpl)mc.get(site);
                    removeGroovyClass(mci.getClassInfo().getTheClass(), metaClassRegistry);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    //e.printStackTrace();
                }
            }
            removeGroovyClass(site.getClass(), metaClassRegistry);
            ((Vector<Class>)classes.get(site.getClass().getClassLoader())).remove(site.getClass());
            cs[i] = null;
        }
    }

    private static void removeGroovyClasses(final Class<?> scriptClass, boolean closeClassloader) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        System.out.println(String.format("Removing script class %s from caches", scriptClass));

        removeCallSites(scriptClass);

        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        final ClassLoader classLoader = scriptClass.getClassLoader();
        if (classLoader instanceof GroovyClassLoader)
        {
            if (closeClassloader)
            {
                try (final CustomGroovyClassLoader groovyClassLoader = (CustomGroovyClassLoader)classLoader)
                {
                    for (Class<?> clazz : groovyClassLoader.getLoadedClasses())
                    {
                        removeGroovyClass(clazz, metaClassRegistry);
                        groovyClassLoader.removeClassFromCache(clazz);
                        ((Vector<Class>)classes.get(groovyClassLoader)).remove(clazz);
                    }
                }
                catch (IOException | IllegalAccessException e)
                {
                    e.printStackTrace(System.err);
                }
            }
            else
            {
                removeGroovyClass(scriptClass, metaClassRegistry);
                ((CustomGroovyClassLoader)classLoader).removeClassFromCache(scriptClass);
                ((Vector<Class>)classes.get(classLoader)).remove(scriptClass);
            }
        }
        else
        {
            System.out.println(String.format("Somehow %s is not a GroovyClassloader", classLoader.toString()));
            removeGroovyClass(scriptClass, metaClassRegistry);
        }

        ReferenceBundle.getHardBundle().getManager().removeStallEntries();
        ReferenceBundle.getWeakBundle().getManager().removeStallEntries();
        ReferenceBundle.getSoftBundle().getManager().removeStallEntries();
        ReferenceBundle.getPhantomBundle().getManager().removeStallEntries();
    }

    private static final GroovyClassLoader loader = new CustomGroovyClassLoader(ClassLoader.getSystemClassLoader());
    private static final Class script0Class = loader.parseClass("return 0");
    private static final CustomCallSiteClassLoader callSiteLoader = new CustomCallSiteClassLoader(script0Class);

    public static Class test(String scriptName) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        System.out.println("Start");
        String simpleScriptBody;
        try (InputStream is = GroovyShellExample.class.getClassLoader().getResourceAsStream(scriptName))
        {
            simpleScriptBody = IOUtils.toString(is, "UTF-8");
        }

        Class scriptClass = loader.parseClass(simpleScriptBody, "Compiled"+System.currentTimeMillis());

        CachedClass cachedClass = ((MetaClassImpl) GroovySystem.getMetaClassRegistry().getMetaClass(scriptClass)).getTheCachedClass();

        System.out.println("CachedCalss "+cachedClass);

        Script script = (Script) scriptClass.newInstance();

        script.setProperty("key", 123L);

        Object result = script.run();
        System.out.println("Result: "+result.getClass()+". "+result);

//        System.in.read();

        return scriptClass;
    }

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
//        classLoaderLeakPreventor.runPreClassLoaderInitiators();

        Class klazz = test("SimpleCallSite.groovy");
        removeGroovyClasses(klazz, false);
        System.gc();

        klazz = test("SimpleScriptWithPrintln.groovy");
        removeGroovyClasses(klazz, false);
        System.gc();

        klazz = test("SimpleCallSite2.groovy");
        removeGroovyClasses(klazz, false);
        System.gc();

        klazz = test("SimpleCallSite3.groovy");
        removeGroovyClasses(klazz, false);
        System.gc();

        klazz = null;
        System.out.println("Class cleared. klazz = "+klazz);
        System.gc();

        System.in.read();
    }
}
