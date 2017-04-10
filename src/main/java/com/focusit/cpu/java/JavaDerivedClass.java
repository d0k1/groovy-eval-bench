package com.focusit.cpu.java;

import com.focusit.cpu.groovy.groovyscript.scriptinheritance.GroovyScriptBase;

/**
 * Created by doki on 02.06.16.
 */
public class JavaDerivedClass extends GroovyScriptBase
{
    public Object run()
    {
        return new testJavaClass().exec(getKey());
    }
}
