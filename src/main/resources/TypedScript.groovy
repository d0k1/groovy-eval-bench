/**
 * Created by doki on 30.05.16.
 */


import com.focusit.cpu.groovy.groovyscript.typedscript.ITypedScript

import static java.lang.Math.PI

class TypedScript implements ITypedScript {
    double test(long key) {
        def a = 10.25;
        def b = 2;
        def x = 122;
        def y = a * x + b + (key * PI);
        return y;
    }
}