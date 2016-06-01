package com.focusit.groovy;

import groovy.lang.Binding;
import groovy.lang.Script;

/**
 * Special base class for scripts.
 * Special binding
 * Special get-/set- Property
 * All of it for avoiding as much as possible work with groovy MetaClass
 * Created by dkirpichenkov on 01.06.16.
 */
public abstract class GroovyStrictScriptBase extends Script {
    private POJOBinding binding;

    @Override
    public Object getProperty(String property) {
        return binding.getProperty(property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        binding.setProperty(property, newValue);
    }

    @Override
    public void setBinding(Binding binding) {
        if (binding instanceof POJOBinding) {
            this.binding = (POJOBinding) binding;
        } else {
            this.binding = new POJOBinding(binding.getVariables());
        }
    }

    @Override
    public Binding getBinding() {
        return binding;
    }

    protected abstract void fillBinding();
}
