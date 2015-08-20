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
package org.lenskit.data.summary;

import org.junit.Test;
import org.lenskit.util.keys.KeyedObjectMap;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RatingSummaryTest {
    @Test
    public void testEmptySummary() {
        KeyedObjectMap<RatingSummary.ItemSummary> items =
                KeyedObjectMap.<RatingSummary.ItemSummary>newBuilder()
                              .build();
        RatingSummary sum = new RatingSummary(3.5, items);
        assertThat(sum.getGlobalMean(), equalTo(3.5));
        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));
    }

    @Test
    public void testSummaryItem() {
        RatingSummary.ItemSummary item = new RatingSummary.ItemSummary(37, 3.9, 100);
        KeyedObjectMap<RatingSummary.ItemSummary> items =
                KeyedObjectMap.<RatingSummary.ItemSummary>newBuilder()
                              .add(item)
                              .build();
        RatingSummary sum = new RatingSummary(3.5, items);
        assertThat(sum.getGlobalMean(), equalTo(3.5));
        assertThat(sum.getItemMean(42), notANumber());
        assertThat(sum.getItemOffset(42), equalTo(0.0));
        assertThat(sum.getItemRatingCount(42), equalTo(0));
        assertThat(sum.getItemMean(37), equalTo(3.9));
        assertThat(sum.getItemOffset(37), closeTo(0.4, 1.0e-6));
        assertThat(sum.getItemRatingCount(37), equalTo(100));
    }
}
