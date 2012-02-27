package org.grouplens.lenskit.eval.config;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.pref.PreferenceDomain;

/**
 * Builder for {@link PreferenceDomain} objects.
 * @review Should this be here, or do we want to provide it in the standard core?
 * @author Michael Ekstrand
 */
public class PreferenceDomainBuilder implements Builder<PreferenceDomain> {
    private Double min;
    private Double max;
    private Double precision;

    public PreferenceDomain build() {
        Preconditions.checkState(min != null, "no minimum set");
        Preconditions.checkState(max != null, "no maximum set");
        if (precision == null) {
            return new PreferenceDomain(min, max);
        } else {
            return new PreferenceDomain(min, max, precision);
        }
    }

    public boolean hasMinimum() {
        return min != null;
    }

    public double getMinimum() {
        Preconditions.checkState(min != null, "no minimum set");
        return min;
    }

    public PreferenceDomainBuilder setMinimum(double v) {
        min = v;
        return this;
    }

    public boolean hasMaximum() {
        return max != null;
    }

    public double getMaximum() {
        Preconditions.checkState(max != null, "no maximum set");
        return max;
    }

    public PreferenceDomainBuilder setMaximum(double v) {
        max = v;
        return this;
    }

    public boolean hasPrecision() {
        return precision != null;
    }

    public double getPrecision() {
        Preconditions.checkState(precision != null, "no precision set");
        return precision;
    }

    public PreferenceDomainBuilder setPrecision(double v) {
        precision = v;
        return this;
    }
}
