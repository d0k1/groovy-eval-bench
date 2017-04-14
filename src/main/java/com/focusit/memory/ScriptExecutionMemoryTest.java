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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by dkirpichenkov on 07.04.17.
 */
public class ScriptExecutionMemoryTest {
    private static final GroovyClassLoader loader = new CustomGroovyClassLoader(ClassLoader.getSystemClassLoader());

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
        initReflectionFields();
    }

    private static void initReflectionFields(){
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

    private static void clearRefsInCachedMethod(CachedMethod method) throws IllegalAccessException {
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

    private static boolean isMethodLinkedToClass(CachedMethod method, CachedClass clazz) throws IllegalAccessException {
        SoftReference ref = (SoftReference) pojoCallSiteConstructor.get(method);
        if (ref != null) {
            Constructor ct = (Constructor) ((SoftReference) pojoCallSiteConstructor.get(method)).get();
            if(ct.getDeclaringClass().equals(clazz.getTheClass()))
                return true;
        }

        ref = (SoftReference) pogoCallSiteConstructor.get(method);
        if (ref != null) {
            Constructor ct = (Constructor) ((SoftReference) pogoCallSiteConstructor.get(method)).get();
            if(ct.getDeclaringClass().equals(clazz.getTheClass()))
                return true;
        }

        ref = (SoftReference) staticCallSiteConstructor.get(method);
        if (ref != null) {
            Constructor ct = (Constructor) ((SoftReference) staticCallSiteConstructor.get(method)).get();
            if(ct.getDeclaringClass().equals(clazz.getTheClass()))
                return true;
        }

        return false;
    }

    private static void clearInstanceMethods(CachedClass cls) throws IllegalAccessException {
        MetaClassRegistryImpl mcri = (MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry();
        FastArray mtds = mcri.getInstanceMethods();
        HashSet<CachedClass> classes = new HashSet<>();
        for(int i=0;i<mtds.size();i++){
            if(mtds.get(i) instanceof GeneratedMetaMethod.Proxy) {
                GeneratedMetaMethod m = (GeneratedMetaMethod) mtds.get(i);
                CachedClass ccls = m.getDeclaringClass();
                CachedMethod[] cachedMethods = (CachedMethod[]) ((LazyReference) methods.get(ccls)).get();
                for(CachedMethod cm:cachedMethods){
                    if(isMethodLinkedToClass(cm, cls)){
                        mtds.remove(i);
                        clearRefsInCachedMethod(cm);
//                        callSiteClassLoader.set(ccls, null);
                    }
                }
            }
        }
    }

    private static void clearCachedClass(CachedClass cls) throws IllegalAccessException {

        if(cls.classInfo!=null && cls.getTheClass()!=null && cls.getCachedSuperClass()!=null
                && cls.getCachedSuperClass().getTheClass()!=null) {
            ((LazyReference) fields.get(cls)).clear();

            if (methods.get(cls) != null) {
                CachedMethod[] cachedMethods = (CachedMethod[]) ((LazyReference) methods.get(cls)).get();
                ((LazyReference) methods.get(cls)).clear();
                for(int i=0;i<cachedMethods.length;i++) {
                    CachedMethod method = cachedMethods[i];
                    clearRefsInCachedMethod(method);
                }
            }

            ((LazyReference) constructors.get(cls)).clear();
            CachedConstructor[] cachedConstructors = (CachedConstructor[]) ((LazyReference) constructors.get(cls)).get();
            for (int i = 0; i < cachedConstructors.length; i++) {
                cachedConstructors[i] = null;
            }

            ((LazyReference) cachedSuperClass.get(cls)).clear();

            ((LazyReference) callSiteClassLoader.get(cls)).clear();

            cachedClass.set(cls, null);
            cls.classInfo = null;
        }
    }

    private static void clearClassInfo(ClassInfo classInfo) throws IllegalAccessException {
        classInfo.setStrongMetaClass(null);
        ((LazyReference)cachedClassRef.get(classInfo)).clear();
        ((LazyReference)artifactClassLoader.get(classInfo)).clear();
    }

    private static void removeGroovyClass(final Class<?> scriptClass, final MetaClassRegistry metaClassRegistry)
    {
        try {
            clearInstanceMethods(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getTheCachedClass());
            clearClassInfo(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getClassInfo());
            clearCachedClass(((MetaClassImpl)metaClassRegistry.getMetaClass(scriptClass)).getTheCachedClass());
        } catch (IllegalAccessException e) {
            e.printStackTrace(System.err);
        }

        metaClassRegistry.removeMetaClass(scriptClass);

        groovyGlobalClassValue.remove(scriptClass);
        Introspector.flushFromCaches(scriptClass);
    }

    private static void clearCallSites(Class<?> scriptClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getCallSiteArray = scriptClass.getDeclaredMethod("$getCallSiteArray", null);
        getCallSiteArray.setAccessible(true);
        CallSite[] cs = (CallSite[]) getCallSiteArray.invoke(scriptClass, null);
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        for(int i=0;i<cs.length;i++){
            CallSite site = cs[i];
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
                    //System.err.println(e.toString());
                }
            }
            removeGroovyClass(site.getClass(), metaClassRegistry);
            ((Vector<Class>)classes.get(site.getClass().getClassLoader())).remove(site.getClass());
            cs[i] = null;
        }
    }

    private static void removeGroovyClasses(final Class<?> scriptClass, boolean closeClassloader) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        System.out.println(String.format("Removing script class %s from caches", scriptClass));

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
                        removeClassFromGroovyClassloader(metaClassRegistry, groovyClassLoader, clazz);
                    }
                }
                catch (IOException | IllegalAccessException e)
                {
                    e.printStackTrace(System.err);
                }
            }
            else
            {
                removeClassFromGroovyClassloader(metaClassRegistry, (CustomGroovyClassLoader)classLoader, scriptClass);
            }
        }
        else
        {
            System.out.println(String.format("Somehow %s is not a GroovyClassloader", classLoader.toString()));
            removeGroovyClass(scriptClass, metaClassRegistry);
        }

        clearGroovyRefs();
    }

    private static void clearGroovyRefs() {
        ReferenceBundle.getHardBundle().getManager().removeStallEntries();
        ReferenceBundle.getWeakBundle().getManager().removeStallEntries();
        ReferenceBundle.getSoftBundle().getManager().removeStallEntries();
        ReferenceBundle.getPhantomBundle().getManager().removeStallEntries();
    }

    private static void removeClassFromGroovyClassloader(MetaClassRegistry metaClassRegistry, CustomGroovyClassLoader groovyClassLoader, Class<?> clazz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        clearCallSites(clazz);
        removeGroovyClass(clazz, metaClassRegistry);
        groovyClassLoader.removeClassFromCache(clazz);
        ((Vector<Class>)classes.get(groovyClassLoader)).remove(clazz);
    }

    public static Class compileAndRun(String scriptName) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
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

        return scriptClass;
    }

    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class klazz;

        klazz = compileAndRun("SimpleCallSite.groovy");
        removeGroovyClasses(klazz, false);

        klazz = compileAndRun("SimpleScriptWithPrintln.groovy");
        removeGroovyClasses(klazz, false);

        klazz = compileAndRun("SimpleCallSite2.groovy");
        removeGroovyClasses(klazz, false);

        klazz = compileAndRun("SimpleCallSite3.groovy");
        removeGroovyClasses(klazz, false);

        klazz = null;
        System.out.println("Class cleared. klazz = " + klazz);

        System.gc();

        System.in.read();
    }
}
