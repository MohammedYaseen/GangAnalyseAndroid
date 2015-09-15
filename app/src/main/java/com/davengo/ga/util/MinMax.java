package com.davengo.ga.util;

/**
 * Created by malkhameri on 11.08.2015.
 */
public class MinMax {
    double min;
    double max;

    public MinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }
}
