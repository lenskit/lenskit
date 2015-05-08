package org.grouplens.lenskit.util;

public final class MathUtils {
    private MathUtils() {}

    /**
     * Check whether a value is zero, using a default epsilon.
     * @param val The value.
     * @return {@code true} if the value is within an epsilon of zero.
     */
    public static boolean isZero(double val) {
        return isZero(val, Double.MIN_NORMAL);
    }

    /**
     * Check whether a value is zero, using a configurable epsilon.
     * @param val The value.
     * @param epsilon The tolerance.
     * @return {@code true} if absolute value of {@code val} is less than {@code epsilon}.
     */
    public static boolean isZero(double val, double epsilon) {
        return Math.abs(val) < epsilon;
    }
}
