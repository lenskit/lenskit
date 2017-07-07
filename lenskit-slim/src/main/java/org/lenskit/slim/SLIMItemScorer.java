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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.results.Results;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;

/**
 * SLIM scorer
 * Set itemScorer to this scorer in recommender configuration
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SLIMItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(SLIMItemScorer.class);

    protected final SLIMModel model;
    private final RatingVectorPDAO rvDAO;
    @Nonnull
    private final UserVectorNormalizer normalizer;


    @Inject
    public SLIMItemScorer(SLIMModel m,
                          RatingVectorPDAO dao,
                          UserVectorNormalizer normlzr) {
        model = m;
        rvDAO = dao;
        normalizer = normlzr;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param items The score vector.
     */
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        logger.debug("scoring {} items for user {} with details", items.size(), user);
        Long2DoubleMap ratings = Long2DoubleSortedArrayMap.create(rvDAO.userRatingVector(user));
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> transform = normalizer.makeTransformation(user, ratings);
        Long2DoubleMap ratingNormalized = transform.apply(ratings);
        Long2DoubleMap resultsNormalized = new Long2DoubleOpenHashMap(items.size());
        List<Result> results = new ArrayList<>();

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            Long2DoubleMap weight = model.getWeights(item);
            double score = Vectors.dotProduct(ratingNormalized, weight);
            resultsNormalized.put(item, score);
//            System.out.println("score class user " + user + " item " + item + ": actual rating, normalized rating and prediction: " + ratings.get(item) + "  " + ratingNormalized.get(item) + "  " + score);
        }

        Long2DoubleMap resultsReversed = transform.unapply(resultsNormalized);

        ObjectIterator<Long2DoubleMap.Entry> resultsIter = resultsReversed.long2DoubleEntrySet().iterator();
        while(resultsIter.hasNext()) {
            Long2DoubleMap.Entry entry = resultsIter.next();
            final long item = entry.getLongKey();
            final double score = entry.getDoubleValue();
            results.add(Results.create(item, score));
        }

//        System.out.println("user " + user + " actual ratings in scorer class: ");
//        System.out.println(LongUtils.frozenMap(ratings));
//        System.out.println("user " + user + " actual prediction in scorer class: ");
//        System.out.println(LongUtils.frozenMap(resultsReversed));
//        System.out.println("user " + user + " normalized ratings in scorer class: ");
//        System.out.println(LongUtils.frozenMap(ratingNormalized));
//        System.out.println("user " + user + " normalized prediction in scorer class: ");
//        System.out.println(LongUtils.frozenMap(resultsNormalized));

        return Results.newResultMap(results);
    }

}
