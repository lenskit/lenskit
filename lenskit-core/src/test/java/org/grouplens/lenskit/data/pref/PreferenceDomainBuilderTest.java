/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
