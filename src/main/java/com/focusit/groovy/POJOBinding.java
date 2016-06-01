package com.focusit.groovy;

import groovy.lang.Binding;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by doki on 31.05.16.
 */
public class POJOBinding extends Binding {
    Map<String, Object> cache = new HashMap<>();

    public void fillFromPojo(Object pojo) {
        try {
            BeanInfo info = Introspector.getBeanInfo(pojo.getClass());
            for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
                cache.put(descriptor.getName(), descriptor.getReadMethod().invoke(pojo, null));
            }
        } catch (Exception e) {
            // cant introspect, so, will use groovy's mop
        }
    }

    public POJOBinding() {
    }

    public POJOBinding(Map variables) {
        cache.putAll(variables);
    }

    public Object get(String name) {
        Object result = cache.get(name);
        return result;
    }

    public void set(String name, Object value) {
        cache.put(name, value);
    }

    @Override
    public Object getVariable(String name) {
        try {
            return get(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object getProperty(String property) {
        return getVariable(property);
    }

    @Override
    public void setVariable(String name, Object value) {
        try {
            set(name, value);
        } catch (Exception e) {
            // can't set value. nothing to do
        }
    }

    @Override
    public boolean hasVariable(String name) {
        return get(name) != null;
    }

    @Override
    public Map getVariables() {
        throw new UnsupportedOperationException("no getVariables in silent binding!");
    }

    @Override
    public void setProperty(String property, Object newValue) {
        setVariable(property, newValue);
    }
}
