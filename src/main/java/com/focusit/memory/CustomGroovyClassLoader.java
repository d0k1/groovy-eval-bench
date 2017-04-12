package com.focusit.memory;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

/**
 * Created by dkirpichenkov on 11.04.17.
 */
public class CustomGroovyClassLoader extends GroovyClassLoader {

    public CustomGroovyClassLoader(ClassLoader loader) {
        super(loader);
    }

    public void removeClassFromCache(Class klazz){
        removeClassCacheEntry(klazz.getName());
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        return new CustomClassCollector(new InnerLoader(this), unit, su);
    }

    public static class CustomClassCollector extends ClassCollector{

        protected CustomClassCollector(InnerLoader cl, CompilationUnit unit, SourceUnit su) {
            super(cl, unit, su);
        }

        @Override
        public GroovyClassLoader getDefiningClassLoader() {
            return (GroovyClassLoader) super.getDefiningClassLoader().getParent();
        }
    }
}
