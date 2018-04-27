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
package org.lenskit.predict.ordrec;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.PrecomputedItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.transform.quantize.Quantizer;
import org.lenskit.transform.quantize.ValueArrayQuantizer;
import org.lenskit.util.collections.LongUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OrdRecRatingPredictorTest {
    private DataAccessObject dao;
    private Quantizer qtz;

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {

//      build ratings
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(42, 1, 2));
        rs.add(Rating.create(42, 2, 1));
        rs.add(Rating.create(42, 3, 3));
        rs.add(Rating.create(42, 4, 3));
        rs.add(Rating.create(42, 5, 1));
        rs.add(Rating.create(42, 6, 2));
        rs.add(Rating.create(42, 7, 2));
        rs.add(Rating.create(42, 8, 3));
        rs.add(Rating.create(42, 9, 1));

        StaticDataSource src = new StaticDataSource();
        src.addSource(rs);
        dao = src.get();
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

        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, dao, qtz);
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

        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, dao, qtz);
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


        OrdRecRatingPredictor ordrec = new OrdRecRatingPredictor(scorer, dao, qtz);
        ResultMap preds = ordrec.predictWithDetails(42, LongUtils.packedSet(10, 11, 12));
        assertThat(preds.getScore(10), equalTo(1.0));
        assertThat(preds.getScore(11), equalTo(2.0));
        assertThat(preds.getScore(12), equalTo(3.0));
    }
}
