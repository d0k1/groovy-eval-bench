package com.focusit.memory;

import org.codehaus.groovy.runtime.callsite.CallSiteClassLoader;

/**
 * Created by dkirpichenkov on 11.04.17.
 */
public class CustomCallSiteClassLoader extends CallSiteClassLoader {

    public CustomCallSiteClassLoader(Class klazz) {
        super(klazz);
    }
}
