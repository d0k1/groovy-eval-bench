package com.focusit.template;

import groovy.lang.Script;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public abstract class TemplateBaseClass extends Script
{
    private Object key;

    public Object getKey()
    {
        return key;
    }

    public void setKey(Object key)
    {
        this.key = key;
    }

    public Object execute()
    {
        return run();
    }
}
