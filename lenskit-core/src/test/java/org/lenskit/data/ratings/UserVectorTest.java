/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.ratings;

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
}
