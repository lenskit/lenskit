/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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