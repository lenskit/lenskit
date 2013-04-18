package org.grouplens.lenskit.util.test;

import org.hamcrest.Matcher;

/**
 * Entry point for extra matchers used by LensKit tests.
 */
public final class ExtraMatchers {
    private ExtraMatchers() {}

    /**
     * Match {@link Double#NaN}.
     * @return A matcher that accepts {@link Double#NaN}.
     */
    public static Matcher<Double> notANumber() {
        return new NotANumberMatcher();
    }
}
