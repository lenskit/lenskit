package org.grouplens.lenskit.norm;

/**
 * Quantizer that uses a range and precision to determine discrete values.
 * Values are rounded to the closest discrete value.
 * @author Michael Ekstrand
 */
public class StepRangeQuantizer extends ValueArrayQuantizer {
    private final double minimum;
    private final double maximum;
    private final double precision;

    static double[] makeValues(double min, double max, double prec) {
        if (max < min) throw new IllegalArgumentException("max less than min");
        double nv = (max - min) / prec;
        int n = (int) nv;
        if (Math.abs(nv - n) > 1.0e-6) {
            n += 1; // one more to cover everything...
        }
        if (n == 0) {
            throw new IllegalArgumentException("range has no elements");
        }
        double[] values = new double[n+1];
        for (int i = 0; i <= n; i++) {
            values[i] = min + (prec*i);
        }
        return values;
    }

    /**
     * Create a new step range quantizer.
     * <p/>
     * For example, to quantize ratings to half-star values on a scale of 0.5-5,
     * use min=0.5, max=5, and prec=0.5.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     * @param prec The precision.
     * @throws IllegalArgumentException if the range specifies no elements.
     */
    public StepRangeQuantizer(double min, double max, double prec) {
        super(makeValues(min, max, prec));
        minimum = min;
        maximum = max;
        precision = prec;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getPrecision() {
        return precision;
    }


}
