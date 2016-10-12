/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;
import org.lenskit.util.math.Vectors;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserVectorTest {
    private final static double EPSILON = 1.0e-6;

    /**
     * Test method for {@link Ratings#userRatingVector(java.util.Collection)}.
     */
    @Test
    public void testUserRatingVector() {
        Collection<Rating> ratings = Lists.newArrayList(
                Rating.create(5, 7, 3.5),
                Rating.create(5, 3, 1.5),
                Rating.create(5, 8, 2)
        );
        Long2DoubleMap v = Ratings.userRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, Vectors.sum(v), EPSILON);

        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        Long2DoubleSortedArrayMap sv = Long2DoubleSortedArrayMap.wrap(SortedKeyIndex.create(keys), values);
        assertEquals(sv, v);
    }

    @Test
    public void testUserRatingVectorDedup() {
        Collection<Rating> ratings = Lists.newArrayList(
                Rating.create(5, 7, 3.5, 7),
                Rating.create(5, 3, 1.5, 3),
                Rating.create(5, 8, 2, 5),
                Rating.create(5, 3, 4, 2)
        );
        Long2DoubleMap v = Ratings.userRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, Vectors.sum(v), EPSILON);

        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        Long2DoubleSortedArrayMap sv = Long2DoubleSortedArrayMap.wrap(SortedKeyIndex.create(keys), values);
        assertEquals(sv, v);
    }
}
