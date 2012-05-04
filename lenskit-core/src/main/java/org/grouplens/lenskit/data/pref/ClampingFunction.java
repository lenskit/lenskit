package org.grouplens.lenskit.data.pref;

import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Function for clamping user-item data, typically a preference or rating.
 * @author Michael Ekstrand
 * @since 0.11
 */
@DefaultImplementation(IdentityClampingFunction.class)
public interface ClampingFunction {
    /**
     * Clamp a value.
     * @param user The user ID.
     * @param item The item ID.
     * @param value The value to clamp.
     * @return The clamped value.
     */
    double apply(long user, long item, double value);
}
