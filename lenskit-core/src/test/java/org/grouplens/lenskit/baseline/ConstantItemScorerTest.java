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
package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConstantItemScorerTest {

    @Test
    public void testSingleScore() {
        ItemScorer pred = new ConstantItemScorer(5);
        assertThat(pred.score(5, 10), equalTo(5.0));
    }

    @Test
    public void testScoreSet() {
        ItemScorer pred = new ConstantItemScorer(5);
        SparseVector v = pred.score(42, LongUtils.packedSet(1, 2, 3, 5, 7));
        assertThat(v.keySet(), contains(1L, 2L, 3L, 5L, 7L));
        assertThat(v.values(), everyItem(equalTo(5.0)));
    }

    @Test
    public void testScoreVector() {
        ItemScorer pred = new ConstantItemScorer(5);
        MutableSparseVector v = MutableSparseVector.create(1, 2, 3, 5, 7);
        pred.score(42, v);
        assertThat(v.keySet(), contains(1L, 2L, 3L, 5L, 7L));
        assertThat(v.values(), everyItem(equalTo(5.0)));
    }
}
