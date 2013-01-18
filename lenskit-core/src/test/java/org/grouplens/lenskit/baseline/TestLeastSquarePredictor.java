/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.ThresholdStoppingCondition;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestLeastSquarePredictor {

    private static final double EPSILON = 1.0e-2;
    private PackedPreferenceSnapshot snapshot;
    private LeastSquaresPredictor predictor;

    @Before
    public void createPredictor() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(2, 1, 3));
        rs.add(Ratings.make(3, 1, 4));
        rs.add(Ratings.make(4, 1, 3));
        rs.add(Ratings.make(5, 1, 5));
        rs.add(Ratings.make(5, 2, 2));
        rs.add(Ratings.make(2, 2, 2));
        rs.add(Ratings.make(2, 3, 3));
        rs.add(Ratings.make(3, 3, 2));
        rs.add(Ratings.make(1, 4, 3));
        rs.add(Ratings.make(3, 4, 2));
        rs.add(Ratings.make(4, 4, 2));
        rs.add(Ratings.make(3, 5, 4));
        rs.add(Ratings.make(4, 5, 5));
        rs.add(Ratings.make(5, 6, 4));
        rs.add(Ratings.make(6, 6, 2));
        rs.add(Ratings.make(1, 6, 3));
        rs.add(Ratings.make(3, 6, 4));

        final EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        final DataAccessObject dao = manager.create();
        final Provider<PackedPreferenceSnapshot> provider = new PackedPreferenceSnapshot.Provider(dao);
        snapshot = provider.get();
        final StoppingCondition stop = new ThresholdStoppingCondition(0.1, 10);

        final double regFactor = 0.001;
        final double lrate = 0.003;
        final Provider<LeastSquaresPredictor> builder =
                new LeastSquaresPredictor.Builder(regFactor, lrate, snapshot, stop);
        predictor = builder.get();
    }

    @Test
    public void testKnownUserItem() {
        final long user = 1;
        SparseVector ratings = snapshot.userRatingVector(user);

        ArrayList<Long> key = new ArrayList<Long>();
        key.add(1L);
        key.add(2L);
        key.add(3L);
        MutableSparseVector output = new MutableSparseVector(key);

        predictor.predict(user, ratings, output, true);

        assertEquals(3.18, output.get(1), EPSILON);
        assertEquals(3.04, output.get(2), EPSILON);
        assertEquals(3.07, output.get(3), EPSILON);
    }

    @Test
    public void testUnknownItem() {
        final long user = 2;
        SparseVector ratings = snapshot.userRatingVector(user);

        ArrayList<Long> key = new ArrayList<Long>();
        key.add(14L);
        key.add(15L);
        key.add(16L);
        MutableSparseVector output = new MutableSparseVector(key);

        predictor.predict(user, ratings, output, true);

        assertEquals(3.07, output.get(14), EPSILON);
        assertEquals(3.07, output.get(15), EPSILON);
        assertEquals(3.07, output.get(16), EPSILON);
    }

    @Test
    public void testUnknownUser() {
        final long user = 11;
        SparseVector ratings = snapshot.userRatingVector(user);

        ArrayList<Long> key = new ArrayList<Long>();
        key.add(4L);
        key.add(5L);
        key.add(6L);
        MutableSparseVector output = new MutableSparseVector(key);

        predictor.predict(user, ratings, output, true);

        assertEquals(3.05, output.get(4), EPSILON);
        assertEquals(3.20, output.get(5), EPSILON);
        assertEquals(3.13, output.get(6), EPSILON);
    }
}
