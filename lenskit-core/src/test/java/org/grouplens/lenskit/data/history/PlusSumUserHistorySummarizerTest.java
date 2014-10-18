/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.data.history;

import com.google.common.collect.Lists;
import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test class for summing plusses.
 */
public class PlusSumUserHistorySummarizerTest {
    UserHistorySummarizer sum;

    @Before
    public void createSummarizer() {
        sum = new PlusSumUserHistorySummarizer();
    }

    @Test
    public void testSummarizeEmpty() {
        SparseVector vec = sum.summarize(History.forUser(42));
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testSummarizeNoPlusses() {
        SparseVector vec = sum.summarize(History.forUser(42, Ratings.make(42, 39, 2.5)));
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testOnePlus() {
        SparseVector vec = sum.summarize(History.forUser(42, Events.plus(42, 39)));
        assertThat(vec.size(), equalTo(1));
        assertThat(vec.get(39), equalTo(1.0));
    }

    @Test
    public void testTwoPlusses() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.plus(42, 39),
                                                         Events.plus(42, 67)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(1.0));
        assertThat(vec.get(67), equalTo(1.0));
    }

    @Test
    public void testRepeatedPlusses() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.plus(42, 39),
                                                         Events.plus(42, 67),
                                                         Events.plus(42, 39)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(2.0));
        assertThat(vec.get(67), equalTo(1.0));
    }

    @Test
    public void testValuedPlusses() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.plus(42, 39),
                                                         Events.multiPlus(42, 67, 402),
                                                         Events.plus(42, 39)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(2.0));
        assertThat(vec.get(67), equalTo(402.0));
    }
}
