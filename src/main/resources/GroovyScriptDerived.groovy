/**
 * Created by dkirpichenkov on 31.05.16.
 */

import static java.lang.Math.PI;

class testClass {
    def a = 10.25;
    def b = 2;
    def x = 122;

    public double exec(long key) {
        def y = a * x + b + (key * PI);
        return y;
    }
}

new testClass().exec(key);

