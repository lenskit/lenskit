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
package org.grouplens.lenskit.data.snapshot;

import it.unimi.dsi.fastutil.longs.LongCollection;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.pref.Preferences;
import org.grouplens.lenskit.indexes.IdIndexMapping;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PackedPreferenceSnapshotTest {

    private PackedPreferenceSnapshot snap;
    private static final double EPSILON = 1.0e-6;

    private static Rating rating(long uid, long iid, double value, long ts) {
        return Rating.create(uid, iid, value, ts);
    }

    private static Preference preference(long uid, long iid, double value) {
        return Preferences.make(uid, iid, value);
    }

    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(rating(1, 7, 4, 1));
        rs.add(rating(3, 7, 3, 1));
        rs.add(rating(4, 7, 5, 1));
        rs.add(rating(4, 7, 4, 2));
        rs.add(rating(5, 7, 3, 1));
        rs.add(rating(6, 7, 5, 1));
        rs.add(rating(1, 8, 4, 1));
        rs.add(rating(1, 8, 5, 2));
        rs.add(rating(3, 8, 3, 1));
        rs.add(rating(4, 8, 2, 1));
        rs.add(rating(5, 8, 3, 1));
        rs.add(rating(5, 8, 5, 2));
        rs.add(rating(6, 8, 5, 1));
        rs.add(rating(7, 8, 2, 1));
        rs.add(rating(1, 9, 3, 1));
        rs.add(rating(3, 9, 4, 1));
        rs.add(rating(4, 9, 5, 1));
        rs.add(rating(7, 9, 2, 1));
        rs.add(rating(7, 9, 3, 2));
        rs.add(rating(4, 10, 4, 1));
        rs.add(rating(7, 10, 4, 1));
        rs.add(rating(1, 11, 5, 1));
        rs.add(rating(3, 11, 5, 2));
        rs.add(rating(4, 11, 5, 1));
        EventDAO dao = EventCollectionDAO.create(rs);
        snap = new PackedPreferenceSnapshotBuilder(dao, new Random()).get();
    }

    @Test
    public void testBasicStats() {
        assertThat(snap.getRatings(), hasSize(20));
    }

    @Test
    public void testGetUserIds() {
        LongCollection users = snap.getUserIds();
        assertTrue(users.contains(1));
        assertTrue(users.contains(3));
        assertTrue(users.contains(4));
        assertTrue(users.contains(5));
        assertTrue(users.contains(6));
        assertTrue(users.contains(7));
    }

    @Test
    public void testGetItemIds() {
        LongCollection items = snap.getItemIds();
        assertEquals(5, items.size());
        assertTrue(items.contains(7));
        assertTrue(items.contains(8));
        assertTrue(items.contains(9));
        assertTrue(items.contains(10));
        assertTrue(items.contains(11));
    }

    @Test
    public void testUserIndex() {
        IdIndexMapping ind = snap.userIndex();
        assertEquals(6, ind.size());
        assertTrue(ind.getIdList().contains(1));
        assertTrue(ind.getIdList().contains(3));
        assertTrue(ind.getIdList().contains(4));
        assertTrue(ind.getIdList().contains(5));
        assertTrue(ind.getIdList().contains(6));
        assertTrue(ind.getIdList().contains(7));
        assertEquals(0, ind.getIndex(1));
        assertEquals(1, ind.getIndex(3));
        assertEquals(2, ind.getIndex(4));
        assertEquals(3, ind.getIndex(5));
        assertEquals(4, ind.getIndex(6));
        assertEquals(5, ind.getIndex(7));
        assertEquals(1, ind.getId(0));
        assertEquals(3, ind.getId(1));
        assertEquals(4, ind.getId(2));
        assertEquals(5, ind.getId(3));
        assertEquals(6, ind.getId(4));
        assertEquals(7, ind.getId(5));
    }

    @Test
    public void testItemIndex() {
        IdIndexMapping ind = snap.itemIndex();
        assertEquals(5, ind.size());
        assertTrue(ind.getIdList().contains(7));
        assertTrue(ind.getIdList().contains(8));
        assertTrue(ind.getIdList().contains(9));
        assertTrue(ind.getIdList().contains(10));
        assertTrue(ind.getIdList().contains(11));
        assertEquals(0, ind.getIndex(7));
        assertEquals(1, ind.getIndex(8));
        assertEquals(2, ind.getIndex(9));
        assertEquals(3, ind.getIndex(10));
        assertEquals(4, ind.getIndex(11));
        assertEquals(7, ind.getId(0));
        assertEquals(8, ind.getId(1));
        assertEquals(9, ind.getId(2));
        assertEquals(10, ind.getId(3));
        assertEquals(11, ind.getId(4));
    }

    @Test
    public void testGetRatings() {
        Collection<IndexedPreference> ratings = snap.getRatings();
        assertEquals(20, ratings.size());
        assertTrue(ratings.contains(preference(1, 7, 4)));
        assertTrue(ratings.contains(preference(3, 7, 3)));
        assertTrue(ratings.contains(preference(4, 7, 4)));
        assertTrue(ratings.contains(preference(5, 7, 3)));
        assertTrue(ratings.contains(preference(6, 7, 5)));
        assertTrue(ratings.contains(preference(1, 8, 5)));
        assertTrue(ratings.contains(preference(3, 8, 3)));
        assertTrue(ratings.contains(preference(4, 8, 2)));
        assertTrue(ratings.contains(preference(5, 8, 5)));
        assertTrue(ratings.contains(preference(6, 8, 5)));
        assertTrue(ratings.contains(preference(7, 8, 2)));
        assertTrue(ratings.contains(preference(1, 9, 3)));
        assertTrue(ratings.contains(preference(3, 9, 4)));
        assertTrue(ratings.contains(preference(4, 9, 5)));
        assertTrue(ratings.contains(preference(7, 9, 3)));
        assertTrue(ratings.contains(preference(4, 10, 4)));
        assertTrue(ratings.contains(preference(7, 10, 4)));
        assertTrue(ratings.contains(preference(1, 11, 5)));
        assertTrue(ratings.contains(preference(3, 11, 5)));
        assertTrue(ratings.contains(preference(4, 11, 5)));
    }

    @Test
    public void testGetUserRatings() {
        Collection<IndexedPreference> ratings = snap.getUserRatings(1);
        assertThat(ratings, hasSize(4));
        assertTrue(ratings.contains(preference(1, 7, 4)));
        assertTrue(ratings.contains(preference(1, 8, 5)));
        assertTrue(ratings.contains(preference(1, 9, 3)));
        assertTrue(ratings.contains(preference(1, 11, 5)));

        ratings = snap.getUserRatings(2);
        assertEquals(0, ratings.size());

        ratings = snap.getUserRatings(3);
        assertEquals(4, ratings.size());
        assertTrue(ratings.contains(preference(3, 7, 3)));
        assertTrue(ratings.contains(preference(3, 8, 3)));
        assertTrue(ratings.contains(preference(3, 9, 4)));
        assertTrue(ratings.contains(preference(3, 11, 5)));

        ratings = snap.getUserRatings(4);
        assertEquals(5, ratings.size());
        assertTrue(ratings.contains(preference(4, 7, 4)));
        assertTrue(ratings.contains(preference(4, 8, 2)));
        assertTrue(ratings.contains(preference(4, 9, 5)));
        assertTrue(ratings.contains(preference(4, 10, 4)));
        assertTrue(ratings.contains(preference(4, 11, 5)));

        ratings = snap.getUserRatings(5);
        assertEquals(2, ratings.size());
        assertTrue(ratings.contains(preference(5, 7, 3)));
        assertTrue(ratings.contains(preference(5, 8, 5)));

        ratings = snap.getUserRatings(6);
        assertEquals(2, ratings.size());
        assertTrue(ratings.contains(preference(6, 7, 5)));
        assertTrue(ratings.contains(preference(6, 8, 5)));

        ratings = snap.getUserRatings(7);
        assertEquals(3, ratings.size());
        assertTrue(ratings.contains(preference(7, 8, 2)));
        assertTrue(ratings.contains(preference(7, 9, 3)));
        assertTrue(ratings.contains(preference(7, 10, 4)));
    }

    @Test
    public void testUserRatingVector() {
        SparseVector ratings = snap.userRatingVector(1);
        assertEquals(4, ratings.size());
        assertEquals(4, ratings.get(7), EPSILON);
        assertEquals(5, ratings.get(8), EPSILON);
        assertEquals(3, ratings.get(9), EPSILON);
        assertEquals(5, ratings.get(11), EPSILON);

        ratings = snap.userRatingVector(2);
        assertEquals(0, ratings.size());

        ratings = snap.userRatingVector(3);
        assertEquals(4, ratings.size());
        assertEquals(3, ratings.get(7), EPSILON);
        assertEquals(3, ratings.get(8), EPSILON);
        assertEquals(4, ratings.get(9), EPSILON);
        assertEquals(5, ratings.get(11), EPSILON);

        ratings = snap.userRatingVector(4);
        assertEquals(5, ratings.size());
        assertEquals(4, ratings.get(7), EPSILON);
        assertEquals(2, ratings.get(8), EPSILON);
        assertEquals(5, ratings.get(9), EPSILON);
        assertEquals(4, ratings.get(10), EPSILON);
        assertEquals(5, ratings.get(11), EPSILON);

        ratings = snap.userRatingVector(5);
        assertEquals(2, ratings.size());
        assertEquals(3, ratings.get(7), EPSILON);
        assertEquals(5, ratings.get(8), EPSILON);

        ratings = snap.userRatingVector(6);
        assertEquals(2, ratings.size());
        assertEquals(5, ratings.get(7), EPSILON);
        assertEquals(5, ratings.get(8), EPSILON);

        ratings = snap.userRatingVector(7);
        assertEquals(3, ratings.size());
        assertEquals(2, ratings.get(8), EPSILON);
        assertEquals(3, ratings.get(9), EPSILON);
        assertEquals(4, ratings.get(10), EPSILON);
    }
}
