import com.focusit.groovyscript.typedscript.ITypedScript

/**
 * Created by doki on 30.05.16.
 */

class TypedScript implements ITypedScript {
    double test(long key) {
        def a = 10.25;
        def b = 2;
        def x = 122;
        def y = a * x + b + key;
        return y;
    }
}