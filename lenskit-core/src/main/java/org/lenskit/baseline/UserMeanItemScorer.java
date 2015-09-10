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
package org.lenskit.baseline;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Rating scorer that returns the user's average rating for all predictions.
 *
 * <p>This scorer does not directly average the user's ratings; rather, it averages their offsets
 * from the scores produced by another scorer (the {@link UserMeanBaseline}).  If this is the
 * {@link GlobalMeanRatingItemScorer} (the default), then this is a straight user mean item
 * scorer with damping; reconfigure it to use {@link ItemMeanRatingItemScorer} as the baseline to
 * get a user-item personalized mean.
 *
 * <p>This is why it is not called a mean <em>rating</em> item scorer; it can compute
 * the mean of any kind of user-based score.
 */
public class UserMeanItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(UserMeanItemScorer.class);

    private final UserEventDAO userEventDAO;
    private final ItemScorer baseline;
    private final UserHistorySummarizer summarizer;
    private final double damping;

    /**
     * Construct a scorer that computes user means offset by the global mean.
     *
     * @param dao  The DAO to get user ratings.
     * @param base An item scorer that provides the baseline scores.
     * @param sum  The summarizer for getting user histories.
     * @param damp A damping term for the calculations.
     */
    @Inject
    public UserMeanItemScorer(UserEventDAO dao,
                              @UserMeanBaseline ItemScorer base,
                              UserHistorySummarizer sum,
                              @MeanDamping double damp) {
        Preconditions.checkArgument(damp >= 0, "Negative damping not allowed");
        userEventDAO = dao;
        baseline = base;
        summarizer = sum;
        damping = damp;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        UserHistory<?> history = userEventDAO.getEventsForUser(user, summarizer.eventTypeWanted());
        if (history == null) {
            Map<Long, Double> scores = baseline.score(user, items);
            return Results.newResultMap(Iterables.transform(scores.entrySet(), Results.fromEntryFunction()));
        } else {
            MutableSparseVector vec = summarizer.summarize(history).mutableCopy();
            // score everything, both rated and not, for offsets
            LongSet allItems = new LongOpenHashSet(vec.keySet());
            allItems.addAll(items);
            Map<Long, Double> baseScores = baseline.score(user, allItems);

            // subtract scores from ratings, yielding offsets
            Long2DoubleFunction bsf = LongUtils.asLong2DoubleFunction(baseScores);
            for (VectorEntry e: vec) {
                double base = bsf.get(e.getKey());
                vec.set(e, e.getValue() - base);
            }

            double meanOffset = vec.sum() / (vec.size() + damping);

            // to score: fill with baselines, add user mean offset
            List<Result> results = new ArrayList<>(items.size());
            LongIterator iter = LongIterators.asLongIterator(items.iterator());
            while (iter.hasNext()) {
                long item = iter.nextLong();
                results.add(Results.create(item, bsf.get(item) + meanOffset));
            }
            return Results.newResultMap(results);
        }
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(%s, γ=%.2f)", cls, baseline, damping);
    }
}
