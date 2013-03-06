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
package org.grouplens.lenskit.util;

import org.grouplens.lenskit.ids.ScoredId;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Ekstrand
 */
public class TestTopNScoredItemAccumulator {
    ScoredItemAccumulator accum;

    @Before
    public void createAccumulator() {
        accum = new TopNScoredItemAccumulator(3);
    }

    @Test
    public void testEmpty() {
        List<ScoredId> out = accum.finish();
        assertTrue(out.isEmpty());
    }

    @Test
    public void testAccum() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        List<ScoredId> out = accum.finish();
        assertThat(out.size(), equalTo(3));
        assertThat(out.get(0).getId(), equalTo(2L));
        assertThat(out.get(0).getScore(), equalTo(9.8));
        assertThat(out.get(1).getId(), equalTo(5L));
        assertThat(out.get(1).getScore(), equalTo(4.2));
        assertThat(out.get(2).getId(), equalTo(3L));
        assertThat(out.get(2).getScore(), equalTo(2.9));
    }

    @Test
    public void testAccumLimit() {
        accum.put(7, 1.0);
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        accum.put(8, 2.1);
        List<ScoredId> out = accum.finish();
        assertThat(out.size(), equalTo(3));
        assertThat(out.get(0).getId(), equalTo(2L));
        assertThat(out.get(0).getScore(), equalTo(9.8));
        assertThat(out.get(1).getId(), equalTo(5L));
        assertThat(out.get(1).getScore(), equalTo(4.2));
        assertThat(out.get(2).getId(), equalTo(3L));
        assertThat(out.get(2).getScore(), equalTo(2.9));
    }
}
