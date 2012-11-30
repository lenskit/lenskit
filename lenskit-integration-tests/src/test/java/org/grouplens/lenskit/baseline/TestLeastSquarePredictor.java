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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;
import org.junit.Before;
import org.junit.Test;

public class TestLeastSquarePredictor {

    private static final double EPSILON = 1.0e-6;
    private PackedPreferenceSnapshot snapshot;
    private LeastSquaresPredictor predictor;
    
    @Before
    public void createPredictor() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 6, 4));
        rs.add(Ratings.make(2, 6, 2));
        rs.add(Ratings.make(1, 7, 3));
        rs.add(Ratings.make(2, 7, 2));
        rs.add(Ratings.make(3, 7, 5));
        rs.add(Ratings.make(4, 7, 2));
        rs.add(Ratings.make(1, 8, 3));
        rs.add(Ratings.make(2, 8, 4));
        rs.add(Ratings.make(3, 8, 3));
        rs.add(Ratings.make(4, 8, 2));
        rs.add(Ratings.make(5, 8, 3));
        rs.add(Ratings.make(6, 8, 2));
        rs.add(Ratings.make(1, 9, 3));
        rs.add(Ratings.make(3, 9, 4));

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
    public void testPrediction1() {
    	final long user = 1;
        SparseVector ratings = snapshot.userRatingVector(user);
        MutableSparseVector output = new MutableSparseVector();
        predictor.predict(user, ratings, output, true);
        
        assertEquals(0.0, output.get(6), EPSILON);
        assertEquals(0.0, output.get(7), EPSILON);
        assertEquals(0.0, output.get(8), EPSILON);
    }

    @Test
    public void testPrediction2() {
    	final long user = 2;
    	SparseVector ratings = snapshot.userRatingVector(user);
        MutableSparseVector output = new MutableSparseVector();
        predictor.predict(user, ratings, output, true);
        
        assertEquals(0.0, output.get(6), EPSILON);
        assertEquals(0.0, output.get(7), EPSILON);
        assertEquals(0.0, output.get(8), EPSILON);
    }
}
