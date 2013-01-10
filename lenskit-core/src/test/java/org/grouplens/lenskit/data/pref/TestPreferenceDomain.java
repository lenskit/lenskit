/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Ekstrand
 */
public class TestPreferenceDomain {
    @Test
    public void testParseContinuous() {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,3.0]");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(3.0, 1.0e-6));
        assertFalse(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(Double.MIN_VALUE));
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
        assertTrue(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(0.5));
    }

    @Test
    public void testParseInt() {
        PreferenceDomain d = PreferenceDomain.fromString("[ 1 , 5 ] / 1");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(5.0, 1.0e-6));
        assertTrue(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(1.0));
    }
}
