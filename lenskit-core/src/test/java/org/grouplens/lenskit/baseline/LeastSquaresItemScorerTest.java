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

import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshotBuilder;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.ThresholdStoppingCondition;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ResultMap;
import org.lenskit.baseline.LeastSquaresItemScorer;
import org.lenskit.util.collections.LongUtils;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class LeastSquaresItemScorerTest {

    private static final double EPSILON = 1.0e-2;
    private PackedPreferenceSnapshot snapshot;
    private LeastSquaresItemScorer predictor;

    @Before
    public void createPredictor() {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(2, 1, 3));
        rs.add(Rating.create(3, 1, 4));
        rs.add(Rating.create(4, 1, 3));
        rs.add(Rating.create(5, 1, 5));
        rs.add(Rating.create(5, 2, 2));
        rs.add(Rating.create(2, 2, 2));
        rs.add(Rating.create(2, 3, 3));
        rs.add(Rating.create(3, 3, 2));
        rs.add(Rating.create(1, 4, 3));
        rs.add(Rating.create(3, 4, 2));
        rs.add(Rating.create(4, 4, 2));
        rs.add(Rating.create(3, 5, 4));
        rs.add(Rating.create(4, 5, 5));
        rs.add(Rating.create(5, 6, 4));
        rs.add(Rating.create(6, 6, 2));
        rs.add(Rating.create(1, 6, 3));
        rs.add(Rating.create(3, 6, 4));

        final EventDAO dao = EventCollectionDAO.create(rs);
        final Provider<PackedPreferenceSnapshot> provider = new PackedPreferenceSnapshotBuilder(dao, new Random());
        snapshot = provider.get();
        final StoppingCondition stop = new ThresholdStoppingCondition(0.1, 10);

        final double regFactor = 0.001;
        final double lrate = 0.003;
        final Provider<LeastSquaresItemScorer> builder =
                new LeastSquaresItemScorer.Builder(regFactor, lrate, snapshot, stop);
        predictor = builder.get();
    }

    @Test
    public void testKnownUserItem() {
        ResultMap scores = predictor.scoreWithDetails(1, LongUtils.packedSet(1, 2, 3));

        assertEquals(3.18, scores.getScore(1), EPSILON);
        assertEquals(3.04, scores.getScore(2), EPSILON);
        assertEquals(3.07, scores.getScore(3), EPSILON);
    }

    @Test
    public void testUnknownItem() {
        ResultMap scores = predictor.scoreWithDetails(2, LongUtils.packedSet(14, 15, 16));

        assertEquals(3.07, scores.getScore(14), EPSILON);
        assertEquals(3.07, scores.getScore(15), EPSILON);
        assertEquals(3.07, scores.getScore(16), EPSILON);
    }

    @Test
    public void testUnknownUser() {
        ResultMap scores = predictor.scoreWithDetails(11, LongUtils.packedSet(4,5,6));

        assertEquals(3.05, scores.getScore(4), EPSILON);
        assertEquals(3.20, scores.getScore(5), EPSILON);
        assertEquals(3.13, scores.getScore(6), EPSILON);
    }
}
