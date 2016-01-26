package org.grouplens.lenskit.mf.svdfeature;


/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ArrayHelper {

    public static double innerProduct(double[] base, double[] other) {
        double prod = 0;
        for (int i=0; i<base.length; i++) {
            prod += (base[i] * other[i]);
        }
        return prod;
    }

    public static void addition(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] += other[i];
        }
    }

    public static void subtraction(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] -= other[i];
        }
    }

    public static void copy(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] = other[i];
        }
    }

    public static void scale(double[] base, double x) {
        for (int i=0; i<base.length; i++) {
            base[i] *= x;
        }
    }

    public static void initialize(double[] base, double val) {
        for (int i=0; i<base.length; i++) {
            base[i] = val;
        }
    }

    public static void randomInitialize(double[] base) {
        for (int i=0; i<base.length; i++) {
            base[i] = Math.random()/10000;
        }
    }

    public static double squaredNorm(double[] base) {
        double normv = 0;
        for (int i=0; i<base.length; i++) {
            normv += base[i] * base[i];
        }
        return normv;
    }

    public static double norm(double[] base) {
        double normv = 0;
        for (int i=0; i<base.length; i++) {
            normv += base[i] * base[i];
        }
        return Math.sqrt(normv);
    }
}
