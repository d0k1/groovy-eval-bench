import static org.codehaus.groovy.runtime.InvokerHelper.toMapString;

class Init {
    static {
        def toStr = {
            "expando: " + toMapString(delegate, 100)
        }

        LinkedHashMap.class.metaClass.toString = toStr;
    }
}


return script.run();
