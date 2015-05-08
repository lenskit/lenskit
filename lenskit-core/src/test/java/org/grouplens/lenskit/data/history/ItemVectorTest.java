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

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemVectorTest {
    private static final double EPSILON = 1.0e-6;

    /**
     * Test method for {@link org.grouplens.lenskit.data.event.Ratings#itemRatingVector(java.util.Collection)}.
     */
    @Test
    public void testItemRatingVector() {
        Collection<Rating> ratings = new ArrayList<Rating>();
        ratings.add(Rating.create(7, 5, 3.5));
        RatingBuilder rb = new RatingBuilder();
        ratings.add(Rating.create(3, 5, 1.5));
        ratings.add(Rating.create(8, 5, 2));
        SparseVector v = Ratings.itemRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, v.sum(), EPSILON);

        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        SparseVector sv = MutableSparseVector.wrap(keys, values);
        assertEquals(sv, v);
    }

}
