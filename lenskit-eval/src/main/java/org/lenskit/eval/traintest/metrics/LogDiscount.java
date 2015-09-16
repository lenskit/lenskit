package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Preconditions;

/**
 * Logarithmic discounting.  All ranks up through the log base have a discount of 1.
 */
public class LogDiscount implements Discount {
    private final double logBase;
    private final double logScaleTerm;

    /**
     * Construct a log discount.
     * @param base The discount base.
     */
    public LogDiscount(double base) {
        Preconditions.checkArgument(base > 1, "base must be greater than 1");
        logBase = base;
        logScaleTerm = 1 / Math.log(base);
    }

    /**
     * Get the log base from the discount.
     * @return The discount's log base.
     */
    public double getLogBase() {
        return logBase;
    }

    @Override
    public double discount(int rank) {
        if (rank <= logBase) {
            return 1;
        } else {
            return Math.log(rank) * logScaleTerm;
        }
    }

    @Override
    public String toString() {
        return "LogDiscount(" + logBase + ")";
    }
}
