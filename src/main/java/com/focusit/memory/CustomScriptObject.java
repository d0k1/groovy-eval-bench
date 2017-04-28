package com.focusit.memory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by dkirpichenkov on 28.04.17.
 */
public class CustomScriptObject implements Map<Object, Object> {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<?, ?> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Object> keySet() {
        return null;
    }

    @Override
    public Collection<Object> values() {
        return null;
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return null;
    }

    @Override
    public String toString() {
        return "CustomScriptObject{}";
    }
}
