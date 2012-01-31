package org.grouplens.lenskit.data.pref;

import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MinRating;

/**
 * An object describing the domain of preference data, used in ratings and predictions.
 *
 * @review Should this be called RatingDomain?
 * @todo Integrate this with {@link MinRating} and {@link MaxRating}.
 * @author Michael Ekstrand
 */
public class PreferenceDomain {
    private final double minimum;
    private final double maximum;
    private final double precision;

    /**
     * Create a discrete bounded preference domain.
     * @param min The minimum preference value.
     * @param max The maximum preference value.
     * @param prec The preference precision.
     */
    public PreferenceDomain(double min, double max, double prec) {
        minimum = min;
        maximum = max;
        precision = prec;
    }

    /**
     * Create a continuous bounded preference domain.
     * @param min The minimum preference value.
     * @param max The maximum preference value.
     */
    public PreferenceDomain(double min, double max) {
        this(min, max, Double.NaN);
    }

    /**
     * The minimum preference value.
     * @return The minimum preference value.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * The maximum preference value.
     * @return The maximum preference value.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * The precision of preference values. This is the precision with which data is
     * collected from the user — in a 1-5, half-star rating system, it will be 0.5.
     * @return The preference precision, or {@code null} if preferences should
     * be considered continuous.
     * @review Is null the correct return? Should we add hasPrecision()?
     */
    public Double getPrecision() {
        if (Double.isNaN(precision)) {
            return null;
        } else {
            return precision;
        }
    }
}
