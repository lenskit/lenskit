package org.grouplens.lenskit.data.pref;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainBuilderTest {
    @Test
    public void testEmptyBuilder() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        try {
            bld.build();
            fail("builder should throw an exception with no arguments");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testParamBuilder() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder(1, 5);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(1.0));
        assertThat(dom.getMaximum(), equalTo(5.0));
        assertThat(dom.getPrecision(), equalTo(0.0));
    }

    @Test
    public void testSetMinMax() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        bld.setMinimum(-1)
           .setMaximum(1);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(-1.0));
        assertThat(dom.getMaximum(), equalTo(1.0));
        assertThat(dom.getPrecision(), equalTo(0.0));
    }

    @Test
    public void testSetAll() {
        PreferenceDomainBuilder bld = new PreferenceDomainBuilder();
        bld.setMinimum(1.0)
           .setMaximum(5)
           .setPrecision(0.5);
        PreferenceDomain dom = bld.build();
        assertThat(dom.getMinimum(), equalTo(1.0));
        assertThat(dom.getMaximum(), equalTo(5.0));
        assertThat(dom.getPrecision(), equalTo(0.5));
    }
}
