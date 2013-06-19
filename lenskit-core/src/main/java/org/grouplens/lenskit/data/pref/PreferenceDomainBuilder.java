package org.grouplens.lenskit.data.pref;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;

/**
 * Build a {@link PreferenceDomain}.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainBuilder implements Builder<PreferenceDomain> {
    private double minimum = Double.NaN;
    private double maximum = Double.NaN;
    private double precision = Double.NaN;

    /**
     * Create an uninitialized preference domain builder.  The minimum and maximum must be provided
     * before the {@link #build()} method may be called.
     */
    public PreferenceDomainBuilder() {}

    /**
     * Create a preference domain builder with a specified minimum and maximum.
     * @param min The minimum preference.
     * @param max The maximum preference.
     */
    public PreferenceDomainBuilder(double min, double max) {
        setMinimum(min);
        setMaximum(max);
    }

    /**
     * Get the minimum preference.
     * @return The minimum preference.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Set the minimum preference.
     * @param minimum The minimum preference.
     * @return The builder (for chaining).
     */
    public PreferenceDomainBuilder setMinimum(double minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Get the maximum preference.
     * @return The maximum preference.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Set the maximum preference.
     * @param maximum The maximum preference.
     * @return The builder (for chaining).
     */
    public PreferenceDomainBuilder setMaximum(double maximum) {
        this.maximum = maximum;
        return this;
    }

    /**
     * Get the preference precision.
     * @return The preference precision.
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Set the preference precision.
     * @param precision The preference precision, or {@link Double#NaN} for unlimited precision.
     * @return The preference precision.
     */
    public PreferenceDomainBuilder setPrecision(double precision) {
        this.precision = precision;
        return this;
    }

    @Override
    public PreferenceDomain build() {
        Preconditions.checkState(!Double.isNaN(minimum), "no minimum preference specified");
        Preconditions.checkState(!Double.isNaN(maximum), "no maximum preference specified");
        return new PreferenceDomain(minimum, maximum, precision);
    }
}
