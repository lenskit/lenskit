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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.transform.threshold.RealThreshold;
import org.grouplens.lenskit.transform.threshold.Threshold;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.data.ratings.StandardRatingVectorPDAO;
import org.lenskit.similarity.CosineVectorSimilarity;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.transform.normalize.*;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.math.Vectors;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class SLIMBuildContextTest {
    private DataAccessObject dao;
    private UserVectorNormalizer normalizer;
    private VectorSimilarity itemSimilarity;
    private Threshold threshold;
    private final int minCommonUsers = 1;
    private final int modelSize = 3;
    private SLIMBuildContextProvider contextProvider;
    private SLIMBuildContext context;
    private Long2ObjectMap<Long2DoubleMap> data;

    @Before
    public void setUp() throws Exception {
        List<Rating> rs = new ArrayList<>();
        double[][] ratings = {
                {4,   0,   1,   3,   0},
                {3.5, 1.0, 1.0, 5,   0},
                {0,   2.0, 3.5, 1.5, 0},
                {0,   1.5, 1,   1,   1.5},
                {2.5, 1,   3.5, 4.5, 0},
                {0,   1,   0,   2,   0,}};
        EntityFactory ef = new EntityFactory();
        data = new Long2ObjectOpenHashMap<>();
        for (int user = 1; user <= ratings.length; user++) {
            double[] userRatings = ratings[user-1];
            for (int item = 1; item <= userRatings.length; item++) {
                double rating = userRatings[item-1];
                if (rating != 0) {
                    rs.add(ef.rating(user, item, rating));
                    Long2DoubleMap itemRatings = data.get(item);
                    if (itemRatings == null) itemRatings = new Long2DoubleOpenHashMap();
                    itemRatings.put(user, rating);
                    data.put(item, itemRatings);
                }
            }
        }

        StaticDataSource source = StaticDataSource.fromList(rs);
        dao = source.get();
        RatingVectorPDAO rvPDAO = new StandardRatingVectorPDAO(dao);
        normalizer = new DefaultUserVectorNormalizer(new UnitVectorNormalizer());
        itemSimilarity = new CosineVectorSimilarity();
        threshold = new RealThreshold(0.0);
        contextProvider = new SLIMBuildContextProvider( rvPDAO, normalizer, itemSimilarity,
                                                        threshold, minCommonUsers, modelSize);
        context = contextProvider.get();

    }


    @Test
    public void testGetItemUniverse() {
        LongSortedSet allItems = context.getItemUniverse();
        assertThat(allItems, containsInAnyOrder(1L, 2L, 3L, 4L, 5L));
    }

    @Test
    public void testGetUserItems() {
        LongSortedSet allUsers = context.getAllUsers();
        assertThat(allUsers, containsInAnyOrder(1L,2L,3L,4L,5L,6L));
        Long2ObjectMap<Long2DoubleMap> dataT = Vectors.transposeMap(data);

        for (long user : allUsers) {
            assertThat(dataT.get(user).keySet(), containsInAnyOrder(context.getUserItems(user).toArray()));
        }
    }

    @Test
    public void testGetItemRatings() {
        Long2ObjectMap<Long2DoubleMap> userViewData = Vectors.transposeMap(data);
        Iterator<Map.Entry<Long,Long2DoubleMap>> iter = data.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,Long2DoubleMap> entry = iter.next();
            final long item = entry.getKey();
            Long2DoubleMap ratings = LongUtils.frozenMap(entry.getValue());
            Long2DoubleMap ratingsNorm = new Long2DoubleOpenHashMap(ratings.size());
            Iterator<Long2DoubleMap.Entry> itemRatingsIter = ratings.long2DoubleEntrySet().iterator();
            while (itemRatingsIter.hasNext()) {
                Long2DoubleMap.Entry ratingEntry = itemRatingsIter.next();
                final long user = ratingEntry.getLongKey();
                final double rating = ratingEntry.getDoubleValue();
                final double norm = Vectors.euclideanNorm(userViewData.get(user));
                final double ratingNorm = rating / norm;
                ratingsNorm.put(user, ratingNorm);
                assertThat(ratingNorm, closeTo(context.getItemRatings(item).get(user), 1.0e-10));
            }
            Long2DoubleMap contextRatings = context.getItemRatings(item);
            Long2DoubleMap ratingsFinal = LongUtils.frozenMap(ratingsNorm);
            System.out.println("actual ratings: " + ratingsFinal);
            System.out.println("context ratings: " + contextRatings);
        }
    }

    @Test
    public void testGetInnerProducts() {
        Long2ObjectMap<Long2DoubleMap> userViewData = Vectors.transposeMap(data);
        Iterator<Map.Entry<Long,Long2DoubleMap>> iter1 = data.entrySet().iterator();
        while (iter1.hasNext()) {
            Map.Entry<Long,Long2DoubleMap> entry = iter1.next();
            final long item1 = entry.getKey();
            Long2DoubleMap ratings1 = entry.getValue();
            Long2DoubleMap ratingsNorm1 = new Long2DoubleOpenHashMap(ratings1.size());
            Iterator<Long2DoubleMap.Entry> itemRatingsIter = ratings1.long2DoubleEntrySet().iterator();
            while (itemRatingsIter.hasNext()) {
                Long2DoubleMap.Entry ratingEntry = itemRatingsIter.next();
                final long user = ratingEntry.getLongKey();
                final double rating = ratingEntry.getDoubleValue();
                final double norm = Vectors.euclideanNorm(userViewData.get(user));
                final double ratingNorm = rating / norm;
                ratingsNorm1.put(user, ratingNorm);
            }

            Iterator<Map.Entry<Long,Long2DoubleMap>> iter2 = data.entrySet().iterator();
            while (iter2.hasNext()) {
                Map.Entry<Long, Long2DoubleMap> entry2 = iter2.next();
                final long item2 = entry2.getKey();
                if (item1 != item2) {
                    Long2DoubleMap ratings2 = entry2.getValue();
                    Long2DoubleMap ratingsNorm2 = new Long2DoubleOpenHashMap(ratings2.size());
                    Iterator<Long2DoubleMap.Entry> itemRatingsIter2 = ratings2.long2DoubleEntrySet().iterator();
                    while (itemRatingsIter2.hasNext()) {
                        Long2DoubleMap.Entry ratingEntry = itemRatingsIter2.next();
                        final long user = ratingEntry.getLongKey();
                        final double rating = ratingEntry.getDoubleValue();
                        final double norm = Vectors.euclideanNorm(userViewData.get(user));
                        final double ratingNorm = rating / norm;
                        ratingsNorm2.put(user, ratingNorm);
                    }
                    double actualDot = Vectors.dotProduct(ratingsNorm1, ratingsNorm2);
                    double contextDot = context.getInnerProducts(item1).get(item2);
                    assertThat(actualDot, closeTo(contextDot, 1.0e-10));
                    System.out.println("actual and context computed inner-products for item pair {" + item1 + ", " + item2 + "} is: {" + actualDot + ", " + contextDot + "}");
                }
            }
        }
    }

    @Test
    public void testItemNeighbors() {
        Iterator<Map.Entry<Long,Long2DoubleMap>> iter = data.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long,Long2DoubleMap> e = iter.next();
            long item = e.getKey();
            Long2DoubleMap sim = context.getInnerProducts(item);
            Long2DoubleAccumulator accum = new TopNLong2DoubleAccumulator(modelSize);
            Iterator<Map.Entry<Long,Double>> simIter = sim.entrySet().iterator();
            while (simIter.hasNext()) {
                Map.Entry<Long,Double> entry = simIter.next();
                long item2 = entry.getKey();
                double value = entry.getValue();
                accum.put(item2, value);
            }
            LongSet neighbors = accum.finishSet();
            assertThat(context.getItemNeighbors(item), containsInAnyOrder(neighbors.toArray()));
        }
    }
}