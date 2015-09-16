package org.lenskit.eval.traintest.metrics;

/**
 * Interface for discounts for rank evaluation.
 */
public interface Discount {
    /**
     * Compute the discount for a rank.
     * @param rank The rank (with the first item having rank 1).
     * @return The discount.  This discount will be **multiplied** by the value, so it should generally go towards 0 as
     * the rank increases.
     */
    double discount(int rank);
}
