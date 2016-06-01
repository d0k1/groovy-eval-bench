package com.focusit.template;

import java.io.IOException;
import java.io.Writer;

import com.focusit.GroovyStrictScriptBase;

/**
 * Created by dkirpichenkov on 31.05.16.
 */
public abstract class TemplateBaseClass extends GroovyStrictScriptBase
{
    private Object key;
    private Writer out;

    public void setOut(Writer out)
    {
        this.out = out;
    }

    public TemplateBaseClass()
    {
    }

    public Object getKey()
    {
        return key;
    }

    public void setKey(Object key)
    {
        this.key = key;
    }

    public Object execute() throws IOException
    {
        try
        {
            fillBinding();
            return run();
        }
        finally
        {
            getOut().flush();
        }
    }

    /**
     * Manually fill binding to get more performance. Otherwise reflection should be used to fill the binding.
     */
    @Override
    protected void fillBinding()
    {
        getBinding().setVariable("key", getKey());
        getBinding().setVariable("out", getOut());
    }

    public Writer getOut()
    {
        return out;
    }
}
