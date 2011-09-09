/*
 * LensKit, a reference implementation of recommender algorithms.
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
/**
 *
 */
package org.grouplens.lenskit.data.dao;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestUserProfileCursor {
    private List<Rating> ratings;
    private Cursor<Rating> ratingCursor;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 2, 5, 3.0));
        ratings.add(new SimpleRating(2, 2, 3, 3.0));
        ratings.add(new SimpleRating(3, 2, 39, 2.5));
        ratings.add(new SimpleRating(4, 5, 7, 2.5));
        ratings.add(new SimpleRating(5, 5, 39, 7.2));
        ratingCursor = Cursors.wrap(ratings);
    }

    @Test
    public void testCursor() {
        Cursor<UserHistory<Rating>> cursor =
            new AbstractDataAccessObject.UserHistoryCursor<Rating>(ratingCursor);
        assertTrue(cursor.hasNext());
        UserHistory<Rating> profile = cursor.next();
        assertTrue(cursor.hasNext());
        assertEquals(2, profile.getUserId());
        assertEquals(3, profile.size());
        profile = cursor.next();
        assertFalse(cursor.hasNext());
        assertEquals(5, profile.getUserId());
        assertEquals(2, profile.size());
    }

}
