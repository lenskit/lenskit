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
package org.lenskit.basic;

import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.junit.Test;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
        config.addComponent(EventCollectionDAO.empty());
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
