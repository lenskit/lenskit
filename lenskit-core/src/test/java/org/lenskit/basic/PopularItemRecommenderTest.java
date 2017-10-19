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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.InteractionStatistics;
import org.lenskit.data.ratings.Rating;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class PopularItemRecommenderTest {
    private DataAccessObject dao;
    InteractionStatistics statistics;
    PopularItemRecommender recommender;

    @Before
    public void setUp() {
        List<Rating> ratings = ImmutableList.of(Rating.create(42, 1, 3.2),
                                                Rating.create(39, 1, 2.4),
                                                Rating.create(42, 2, 2.5));
        StaticDataSource source = StaticDataSource.fromList(ratings);
        dao = source.get();
        statistics = InteractionStatistics.create(dao);
        recommender = new PopularItemRecommender(statistics, dao);
    }

    @Test
    public void testRecommendUnratedItems() {
        List<Long> results = recommender.recommend(39);
        assertThat(results, contains(2L));
    }

    @Test
    public void testRecommendAllItems() {
        List<Long> results = recommender.recommend(17);
        assertThat(results, contains(1L, 2L));
    }

    @Test
    public void testRecommendRelatedItems() {
        List<Long> results = recommender.recommendRelatedItems(1);
        assertThat(results, contains(2L));
    }

    @Test
    public void testRecommendAllRelatedItems() {
        List<Long> results = recommender.recommendRelatedItems(17);
        assertThat(results, contains(1L, 2L));
    }

    @Test
    public void testRecommendRelatedBasket() {
        List<Long> results = recommender.recommendRelatedItems(ImmutableSet.of(1L, 10L));
        assertThat(results, contains(2L));
    }

    @Test
    public void testRecommendRelatedWithCandidates() {
        List<Long> results = recommender.recommendRelatedItems(ImmutableSet.of(), -1, ImmutableSet.of(1L), null);
        assertThat(results, contains(1L));
    }

    @Test
    public void testRecommendWithCandidates() {
        List<Long> results = recommender.recommend(10, -1, ImmutableSet.of(1L), null);
        assertThat(results, contains(1L));
    }

    @Test
    public void testRecommendRelatedWithDetails() {
        ResultList results = recommender.recommendRelatedItemsWithDetails(ImmutableSet.of(), -1, null, null);
        assertThat(results.idList(), contains(1L, 2L));
    }

    @Test
    public void testRecommendForUserWithDetails() {
        ResultList results = recommender.recommendWithDetails(17, -1, null, null);
        assertThat(results.idList(), contains(1L, 2L));
    }

    @Test
    public void testRecommendRelatedLimit() {
        List<Long> results = recommender.recommendRelatedItems(100, 1);
        assertThat(results, contains(1L));
    }

    @Test
    public void testRecommendLimit() {
        List<Long> results = recommender.recommend(100, 1);
        assertThat(results, contains(1L));
    }
}