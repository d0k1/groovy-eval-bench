package com.focusit.groovyscript.scriptinheritance;

import groovy.lang.Script;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public abstract class GroovyScriptBase extends Script
{
    private long key;

    public void setKey(long key)
    {
        this.key = key;
    }

    public long getKey()
    {
        return key;
    }

    public Object execute()
    {
        return run();
    }
}
