package com.focusit.java;

import static java.lang.Math.PI;

/**
 * Created by doki on 02.06.16.
 */
public class testJavaClass {
    double a = 10.25;
    double b = 2;
    double x = 122;

    public double exec(long key) {
        double y = a * x + b + (key * PI);
        return y;
    }
}
