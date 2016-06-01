/**
 * Created by doki on 01.06.16.
 */

import static java.lang.Math.PI;

class testStaticClass {
    double a = 10.25;
    int b = 2;
    int x = 122;

    public double exec(long key) {
        double y = a * x + b + (key * PI);
        return y;
    }
}

new testStaticClass().exec(key as long);

