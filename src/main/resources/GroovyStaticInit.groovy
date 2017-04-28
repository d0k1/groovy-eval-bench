import static org.codehaus.groovy.runtime.InvokerHelper.toMapString;
class Init{

    static{
        def toStr = {
            delegate.title != null ? delegate.title : delegate.uuid != null ? delegate.uuid : toMapString(delegate, 10)
        }
        java.util.LinkedHashMap.metaClass.toString = toStr
        com.focusit.memory.CustomScriptObject.metaClass.toString = toStr
    }
}
new Init()

wrapped.run();