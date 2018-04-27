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
package org.lenskit.basic;

import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ConstantItemScorerTest {

    @Test
    public void testSingleScore() {
        ItemScorer pred = new ConstantItemScorer(5);
        assertThat(pred.score(5, 10), equalTo((Result) Results.create(10, 5.0)));
    }

    @Test
    public void testScoreSet() {
        ItemScorer pred = new ConstantItemScorer(5);
        Map<Long, Double> v = pred.score(42, LongUtils.packedSet(1, 2, 3, 5, 7));
        assertThat(v.keySet(), contains(1L, 2L, 3L, 5L, 7L));
        assertThat(v.values(), everyItem(equalTo(5.0)));
    }

    @Test
    public void testScoreDetails() {
        ItemScorer pred = new ConstantItemScorer(5);
        ResultMap v = pred.scoreWithDetails(42, LongUtils.packedSet(1, 2, 3, 5, 7));
        assertThat(v.keySet(), contains(1L, 2L, 3L, 5L, 7L));
        assertThat(v.scoreMap().values(), everyItem(equalTo(5.0)));
    }

    @Test
    public void testInject() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.addComponent(EntityCollectionDAO.create());
        config.bind(ItemScorer.class).to(ConstantItemScorer.class);
        config.set(ConstantItemScorer.Value.class).to(Math.PI);

        try (LenskitRecommender rec = LenskitRecommenderEngine.build(config).createRecommender()) {
            ItemScorer scorer = rec.getItemScorer();
            assertThat(scorer, notNullValue());
            assertThat(scorer, instanceOf(ConstantItemScorer.class));

            Map<Long, Double> v = scorer.score(42, LongUtils.packedSet(1, 2, 3, 5, 7));
            assertThat(v.keySet(), hasSize(5));
            assertThat(v.keySet(), containsInAnyOrder(1L, 2L, 3L, 5L, 7L));
            assertThat(v.values(), everyItem(equalTo(Math.PI)));
        }
    }
}
