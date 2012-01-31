package org.grouplens.lenskit.norm;

import java.io.Serializable;

/**
 * Abstract quantizer implementation using a pre-generated array of possible
 * values. Values are quantized to their closest discrete possibility.
 * @author Michael Ekstrand
 */
public class ValueArrayQuantizer implements Quantizer, Serializable {
    protected final double[] values;

    /**
     * Construct a new quantizer using the specified array of values.
     * @param vs The discrete values to quantize to.
     */
    public ValueArrayQuantizer(double[] vs) {
        if (vs.length <= 0) throw new IllegalArgumentException("must have at least one value");
        values = vs;
    }
    @Override
    public double[] getValues() {
        return values;
    }

    @Override
    public double getValue(int i) {
        try {
            return values[i];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid discrete value", e);
        }
    }

    @Override
    public int apply(double v) {
        final int n = values.length;
        assert n > 0;
        int closest = -1;
        double closev = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double diff = Math.abs(v - values[i]);
            if (diff <= closev) { // <= to round up
                closev = diff;
                closest = i;
            }
        }
        if (closest < 0) {
            throw new RuntimeException("could not quantize value");
        } else {
            return closest;
        }
    }

    @Override
    public int getCount() {
        return values.length;
    }
}
