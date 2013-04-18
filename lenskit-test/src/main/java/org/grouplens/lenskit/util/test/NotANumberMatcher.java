package org.grouplens.lenskit.util.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Matcher for {@link Double#NaN}.
 */
class NotANumberMatcher extends BaseMatcher<Double> {

    @Override
    public boolean matches(Object item) {
        if (item instanceof Double) {
            return Double.isNaN((Double) item);
        } else {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("NaN");
    }
}
