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
package org.lenskit.predict.ordrec;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.PrefetchingUserEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.PrecomputedItemScorer;
import org.lenskit.transform.quantize.Quantizer;
import org.lenskit.transform.quantize.ValueArrayQuantizer;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class OrdRecRatingPredictorTest {
    private EventDAO dao;
    private UserEventDAO userDAO;
    private Quantizer qtz;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {

//      build ratings
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(42, 1, 2));
        rs.add(Rating.create(42, 2, 1));
        rs.add(Rating.create(42, 3, 3));
        rs.add(Rating.create(42, 4, 3));
        rs.add(Rating.create(42, 5, 1));
        rs.add(Rating.create(42, 6, 2));
        rs.add(Rating.create(42, 7, 2));
        rs.add(Rating.create(42, 8, 3));
        rs.add(Rating.create(42, 9, 1));

        dao = new EventCollectionDAO(rs);
        userDAO = new PrefetchingUserEventDAO(dao);
        qtz = new ValueArrayQuantizer(new double[]{1.0, 2.0, 3.0});

    }

    /**
     * This test is to test the basic performance of OrdRecRatingPredictor,
     * The rating value is 1, 2, 3. The score for rating 1 is around 2, for rating 2
     * is around 5, for rating 8 is around 3. So for the Ordrec predictor, given a specific
     * score value, and test if it can return a matched rating.
     */
    @Test
    public void testOrdRecPrediction1() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                .addScore(42, 1, 5)
                .addScore(42, 2, 2)
                .addScore(42, 3, 8)
                .addScore(42, 4, 8.2)
                .addScore(42, 5, 2.1)
                .addScore(42, 6, 4.9)
                .addScore(42, 7, 5)
                .addScore(42, 8, 8)
                .addScore(42, 9, 2)
                .addScore(42, 10, 1.9)
                .addScore(42, 11, 4.8)
                .addScore(42, 12, 8.2)
                .build();

        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, userDAO, qtz);
        ResultMap preds = ordrec.predictWithDetails(42, LongUtils.packedSet(10, 11, 12));
        assertThat(preds.getScore(10), equalTo(1.0));
        assertThat(preds.getScore(11), equalTo(2.0));
        assertThat(preds.getScore(12), equalTo(3.0));
    }

    /**
     * This test is to test the basic performance of OrdRecRatingPredictor,
     * The rating value is 1, 2, 3. The score for rating 1 is around 1, for rating 2
     * is around 2, for rating 3 is around 3. So for the Ordrec predictor, given a specific
     * score value, and test if it can return a matched rating.
     */
    @Test
    public void testOrdRecPrediction2() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                .addScore(42, 1, 2)
                .addScore(42, 2, 1)
                .addScore(42, 3, 3)
                .addScore(42, 4, 3)
                .addScore(42, 5, 1)
                .addScore(42, 6, 2)
                .addScore(42, 7, 2)
                .addScore(42, 8, 3)
                .addScore(42, 9, 1)
                .addScore(42, 10, 1.1)
                .addScore(42, 11, 1.9)
                .addScore(42, 12, 3.1)
                .build();

        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, userDAO, qtz);
        ResultMap preds = ordrec.predictWithDetails(42, LongUtils.packedSet(10, 11, 12));
        assertThat(preds.getScore(10), equalTo(1.0));
        assertThat(preds.getScore(11), equalTo(2.0));
        assertThat(preds.getScore(12), equalTo(3.0));
    }

    /**
     * This test is to test the basic performance of OrdRecRatingPredictor,
     * The rating value is 1, 2, 3. The score for rating 1 is around 0.2, for rating 2
     * is around 1, for rating 3 is around 1.8. So for the Ordrec predictor, given a specific
     * score value, and test if it can return a matched rating.
     */
    @Test
    public void testOrdRecPrediction3() {
        ItemScorer scorer = PrecomputedItemScorer.newBuilder()
                .addScore(42, 1, 1)
                .addScore(42, 2, 0.2)
                .addScore(42, 3, 1.8)
                .addScore(42, 4, 1.8)
                .addScore(42, 5, 0.2)
                .addScore(42, 6, 1)
                .addScore(42, 7, 1)
                .addScore(42, 8, 1.8)
                .addScore(42, 9, 0.2)
                .addScore(42, 10, 0.21)
                .addScore(42, 11, 1.01)
                .addScore(42, 12, 1.75)
                .build();


        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, userDAO, qtz);
        ResultMap preds = ordrec.predictWithDetails(42, LongUtils.packedSet(10, 11, 12));
        assertThat(preds.getScore(10), equalTo(1.0));
        assertThat(preds.getScore(11), equalTo(2.0));
        assertThat(preds.getScore(12), equalTo(3.0));
    }
}
