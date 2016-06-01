package com.focusit.groovyscript.scriptinheritance;

import com.focusit.GroovyStrictScriptBase;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public abstract class GroovyScriptBase extends GroovyStrictScriptBase
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

    public void fillBinding()
    {
        getBinding().setVariable("key", getKey());
    }

    public Object execute()
    {
        fillBinding();
        return run();
    }
}
