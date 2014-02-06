/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A user in a test set, with the results of their recommendations or predictions.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitTestUser extends AbstractTestUser {
    private final LenskitRecommender recommender;
    private final UserHistory<Event> testHistory;
    private final LoadingCache<RecommendRequest,Optional<List<ScoredId>>> recommendCache =
            CacheBuilder.newBuilder().build(new RecommendLoader());
    private transient SparseVector predictionCache = null;

    /**
     * Construct a new test user.
     *
     * @param rec The LensKit recommender.
     * @param testData The user history to test.
     */
    public LenskitTestUser(LenskitRecommender rec, UserHistory<Event> testData) {
        recommender = rec;
        testHistory = testData;
    }

    @Override
    public UserHistory<Event> getTrainHistory() {
        UserHistory<Event> history = recommender.get(UserEventDAO.class).getEventsForUser(getUserId());
        if (history == null) {
            history = History.forUser(getUserId());
        }
        return history;
    }
  
    @Override
    public UserHistory<Event> getTestHistory() {
        return testHistory;
    }

    @Override
    public SparseVector getPredictions() {
        if (predictionCache == null) {
            RatingPredictor pred = recommender.getRatingPredictor();
            if (pred != null) {
                predictionCache = pred.predict(getUserId(), getTestRatings().keySet());
            }
        }
        return predictionCache;
    }

    @Override
    public List<ScoredId> getRecommendations(int n, ItemSelector candSel, ItemSelector exclSel) {
        try {
            return recommendCache.get(new RecommendRequest(n, candSel, exclSel)).orNull();
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public LenskitRecommender getRecommender() {
        return recommender;
    }

    private static class RecommendRequest {
        final int listSize;
        final ItemSelector candidates;
        final ItemSelector exclude;

        private RecommendRequest(int listSize, ItemSelector candidates, ItemSelector exclude) {
            this.listSize = listSize;
            this.candidates = candidates;
            this.exclude = exclude;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) {
                return true;
            } else if (that instanceof RecommendRequest) {
                RecommendRequest rr = (RecommendRequest) that;
                return new EqualsBuilder().append(listSize, rr.listSize)
                                          .append(candidates, rr.candidates)
                                          .append(exclude, rr.exclude)
                                          .isEquals();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(listSize)
                                        .append(candidates)
                                        .append(exclude)
                                        .toHashCode();
        }
    }

    private class RecommendLoader extends CacheLoader<RecommendRequest,Optional<List<ScoredId>>> {
        @Override
        public Optional<List<ScoredId>> load(RecommendRequest key) throws Exception {
            ItemDAO idao = recommender.get(ItemDAO.class);
            if (idao == null ) {
                throw new RuntimeException("cannot recommend without item DAO");
            }
            ItemRecommender irec = recommender.getItemRecommender();
            if (irec == null) {
                return Optional.absent();
            }
            LongSet candidates = key.candidates.select(getTrainHistory(), getTestHistory(), idao.getItemIds());
            LongSet excludes = key.exclude.select(getTrainHistory(), getTestHistory(), idao.getItemIds());
            return Optional.of(irec.recommend(getUserId(), key.listSize, candidates, excludes));
        }
    }
}
