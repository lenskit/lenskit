package org.grouplens.lenskit.data.pref;

import java.io.Serializable;

/**
 * Identity clamping function.
 * @author Michael Ekstrand
 * @since 0.11
 */
public final class IdentityClampingFunction implements ClampingFunction, Serializable {
    private static final long serialVersionUID = 1L;

    public double apply(long user, long item, double value) {
        return value;
    }
}
