/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class TestItemItemModelAccumulator {
    LongSortedSet universe;
    /**
     * An accumulator with a size of 5 and universe of [1,10].
     */
    ItemItemModelAccumulator accum;

    @Before
    public void newAccumulator() {
        universe = new LongAVLTreeSet();
        for (long i = 1; i <= 10; i++) {
            universe.add(i);
        }
        accum = new ItemItemModelAccumulator(5, universe);
    }

    @Test
    public void testEmpty() {
        ItemItemModel model = accum.build();
        assertThat(model, notNullValue());
        assertThat(model.getItemUniverse(), equalTo(universe));
    }
    
    @Test
    public void testAccum() {
        accum.put(1, 2, Math.PI);
        accum.put(7, 3, Math.E);
        ItemItemModel model = accum.build();
        ScoredLongList nbrs = model.getNeighbors(1);
        assertThat(nbrs.size(), equalTo(1));
        assertThat(nbrs.get(0), equalTo(2L));
        assertThat(nbrs.getScore(0), closeTo(Math.PI, 1.0e-6));
        nbrs = model.getNeighbors(7);
        assertThat(nbrs.size(), equalTo(1));
        assertThat(nbrs.get(0), equalTo(3L));
        assertThat(nbrs.getScore(0), closeTo(Math.E, 1.0e-6));
    }

    @Test
    public void testTruncate() {
        for (long i = 1; i <= 10; i++) {
            for (long j = 1; j <= 10; j += (i % 3) + 1) {
                accum.put(i, j, Math.pow(Math.E, -i) * Math.pow(Math.PI, -j));
            }
        }
        ItemItemModel model = accum.build();
        ScoredLongList nbrs = model.getNeighbors(1);
        assertThat(nbrs.size(), equalTo(5));
        nbrs = model.getNeighbors(4);
        assertThat(nbrs.size(), equalTo(5));
        ScoredLongListIterator iter = nbrs.iterator();
        while (iter.hasNext()) {
            long j = iter.nextLong();
            double s = iter.getScore();
            assertThat(s, closeTo(Math.pow(Math.E, -4) * Math.pow(Math.PI, -j), 1.0e-6));
        }
    }
}
