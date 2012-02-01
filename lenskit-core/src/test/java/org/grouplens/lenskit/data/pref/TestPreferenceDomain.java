package org.grouplens.lenskit.data.pref;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class TestPreferenceDomain {
    @Test
    public void testParseContinuous() {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,3.0]");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(3.0, 1.0e-6));
        assertThat(d.getPrecision(), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInverted() {
        PreferenceDomain.fromString("[2.5, -1]");
    }

    @Test
    public void testParseDiscrete() {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,5.0]/0.5");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(5.0, 1.0e-6));
        assertThat(d.getPrecision(), equalTo(0.5));
    }

    @Test
    public void testParseInt() {
        PreferenceDomain d = PreferenceDomain.fromString("[ 1 , 5 ] / 1");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(5.0, 1.0e-6));
        assertThat(d.getPrecision(), equalTo(1.0));
    }
}
