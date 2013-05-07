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
package org.grouplens.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.transform.threshold.RealThreshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestItemItemModelAccumulator {
    LongSortedSet universe;

    public ItemItemModelBuilder.Accumulator simpleAccumulator() {
        universe = new LongAVLTreeSet();
        for (long i = 1; i <= 10; i++) {
            universe.add(i);
        }
        return new ItemItemModelBuilder.Accumulator(universe, new RealThreshold(0.0), 5);
    }

    @Test
    public void testSimpleEmpty() {
        ItemItemModelBuilder.Accumulator accum = simpleAccumulator();
        ItemItemModel model = accum.build();
        assertThat(model, notNullValue());
        assertThat(model.getItemUniverse(), equalTo(universe));
    }

    @Test
    public void testSimpleAccum() {
        ItemItemModelBuilder.Accumulator accum = simpleAccumulator();
        accum.put(1, 2, Math.PI);
        accum.put(7, 3, Math.E);
        ItemItemModel model = accum.build();
        ImmutableSparseVector nbrs = model.getNeighbors(1);
        assertThat(nbrs.size(), equalTo(1));
        assertTrue(nbrs.containsKey(2));
        assertThat(nbrs.get(2), closeTo(Math.PI, 1.0e-6));
        nbrs = model.getNeighbors(7);
        assertThat(nbrs.size(), equalTo(1));
        assertThat(nbrs.keySet(), contains(3L));
        assertThat(nbrs.get(3), closeTo(Math.E, 1.0e-6));
    }

    @Test
    public void testSimpleTruncate() {
        ItemItemModelBuilder.Accumulator accum = simpleAccumulator();
        for (long i = 1; i <= 10; i++) {
            for (long j = 1; j <= 10; j += (i % 3) + 1) {
                accum.put(i, j, Math.pow(Math.E, -i) * Math.pow(Math.PI, -j));
            }
        }
        ItemItemModel model = accum.build();
        ImmutableSparseVector nbrs = model.getNeighbors(1);
        assertThat(nbrs.size(), equalTo(5));
        nbrs = model.getNeighbors(4);
        assertThat(nbrs.size(), equalTo(5));
        for (VectorEntry e : nbrs.fast()) {
            long j = e.getKey();
            double s = e.getValue();
            assertThat(s, closeTo(Math.pow(Math.E, -4) * Math.pow(Math.PI, -j), 1.0e-6));
        }
    }
}
