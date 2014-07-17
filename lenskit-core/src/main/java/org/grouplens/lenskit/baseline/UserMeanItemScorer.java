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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;

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
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
        userEventDAO = dao;
        baseline = base;
        summarizer = sum;
        damping = damp;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector items) {
        UserHistory<?> history = userEventDAO.getEventsForUser(user, summarizer.eventTypeWanted());
        if (history == null) {
            baseline.score(user, items);
        } else {
            MutableSparseVector vec = summarizer.summarize(history).mutableCopy();
            // score everything, both rated and not, for offsets
            LongSet allItems = LongUtils.setUnion(vec.keySet(), items.keyDomain());
            MutableSparseVector baseScores = MutableSparseVector.create(allItems);
            baseline.score(user, baseScores);
            // subtract scores from ratings, yielding offsets
            vec.subtract(baseScores);
            double meanOffset = vec.sum() / (vec.size() + damping);
            // to score: fill with baselines, add user mean offset
            items.set(baseScores);
            items.add(meanOffset);
        }
    }

    @Override
    public String toString() {
        String cls = getClass().getSimpleName();
        return String.format("%s(%s, γ=%.2f)", cls, baseline, damping);
    }
}
