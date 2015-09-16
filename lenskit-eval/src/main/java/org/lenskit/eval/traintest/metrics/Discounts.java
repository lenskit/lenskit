package org.lenskit.eval.traintest.metrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definitions of different discount functions.
 */
public final class Discounts {
    private Discounts() {}

    private static final Pattern LOG_PAT = Pattern.compile("log(?:\\((\\d+)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXP_PAT = Pattern.compile("exp\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Create a log-base-2 discount.
     * @return The discount.
     */
    public static LogDiscount log2() {
        return new LogDiscount(2);
    }

    /**
     * Create a new logarithmic discount.
     * @param base The log base.
     * @return The discount.
     */
    public static LogDiscount log(double base) {
        return new LogDiscount(base);
    }

    /**
     * Create a new exponential (half-life) discount.
     * @param hl The half-life of the decay function.
     * @return The discount.
     */
    public static ExponentialDiscount exp(double hl) {
        return new ExponentialDiscount(hl);
    }

    /**
     * Parse a discount expression from a string.
     * @param disc The discount string.
     * @return The discount.
     */
    public static Discount parse(String disc) {
        if (disc.toLowerCase().equals("log2")) {
            return log2();
        }

        Matcher m = LOG_PAT.matcher(disc);
        if (m.matches()) {
            String grp = m.group(1);
            double base = grp != null ? Double.parseDouble(grp) : 2;
            return new LogDiscount(base);
        }

        m = EXP_PAT.matcher(disc);
        if (m.matches()) {
            double hl = Double.parseDouble(m.group(1));
            return new ExponentialDiscount(hl);
        }

        throw new IllegalArgumentException("invalid discount specification " + disc);
    }
}
