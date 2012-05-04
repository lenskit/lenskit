package org.grouplens.lenskit.data.pref;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Clamp values to the range of valid ratings. This clamping function uses
 * the {@link PreferenceDomain} to clamp values to fall within the minimum
 * and maximum allowable ratings.
 *
 * @author Michael Ekstrand
 * @since 0.11
 */
public class RatingRangeClampingFunction implements ClampingFunction, Serializable {
    private static final long serialVersionUID = 1L;

    private final PreferenceDomain domain;

    @Inject
    public RatingRangeClampingFunction(PreferenceDomain dom) {
        domain = dom;
    }

    public double apply(long user, long item, double value) {
        return domain.clampValue(value);
    }
}
