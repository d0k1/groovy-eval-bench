import static org.codehaus.groovy.runtime.InvokerHelper.toMapString;

class Init1 {
    static {
        def toStr = {
            "expando: " + toMapString(delegate, 100)
        }

        LinkedHashMap.class.metaClass.toString = toStr;
    }
}

new Init1();

return script.run();
