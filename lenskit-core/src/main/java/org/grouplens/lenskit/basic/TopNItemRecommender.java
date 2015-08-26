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
package org.grouplens.lenskit.basic;


import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Recommender that recommends the top N items by a scorer.
 * Implements all methods required by {@link AbstractItemRecommender}. The
 * default exclude set is all items rated by the user.
 *
 * <p>Recommendations are returned in descending order of score.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class TopNItemRecommender extends AbstractItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(TopNItemRecommender.class);
    protected final UserEventDAO userEventDAO;
    protected final ItemDAO itemDAO;
    protected final ItemScorer scorer;

    @Inject
    public TopNItemRecommender(UserEventDAO uedao, ItemDAO idao, ItemScorer scorer) {
        userEventDAO = uedao;
        itemDAO = idao;
        this.scorer = scorer;
    }
    
    public ItemScorer getScorer() {
        return scorer;
    }

    /**
     * Implement the ID-based recommendation in terms of the scorer. This method
     * uses {@link #getDefaultExcludes(long)} to supply a missing exclude set.
     */
    @Override
    protected List<ScoredId> recommend(long user, int n, LongSet candidates, LongSet exclude) {
        if (candidates == null) {
            candidates = getPredictableItems(user);
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(user);
        }
        if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(candidates, exclude);
        }
        logger.debug("Computing {} recommendations for user {} from {} candidates",
                     n, user, candidates.size());

        SparseVector scores = scorer.score(user, candidates);
        return recommend(n, scores);
    }

    /**
     * Pick the top <var>n</var> items from a score vector.
     *
     * @param n      The number of items to recommend.
     * @param scores The scored item vector.
     * @return The top <var>n</var> items from <var>scores</var>, in descending
     *         order of score.
     */
    @SuppressWarnings("unchecked")
    protected List<ScoredId> recommend(int n, SparseVector scores) {
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        if (n < 0) {
            n = scores.size();
        }

        ScoredItemAccumulator accum = new TopNScoredItemAccumulator(n);
        for (VectorEntry pred : scores) {
            final double v = pred.getValue();
            accum.put(pred.getKey(), v);
        }

        List<ScoredId> results = accum.finish();
        if (!scores.getChannelSymbols().isEmpty()) {
            ScoredIdListBuilder builder = ScoredIds.newListBuilder(results.size());
            List<Pair<Symbol,SparseVector>> cvs = Lists.newArrayList();
            List<Pair<TypedSymbol<?>, Long2ObjectMap<?>>> channels = Lists.newArrayList();
            for (Symbol sym: scores.getChannelVectorSymbols()) {
                builder.addChannel(sym, Double.NaN);
                cvs.add(Pair.of(sym, scores.getChannelVector(sym)));
            }
            for (TypedSymbol<?> sym: scores.getChannelSymbols()) {
                if (!sym.getType().equals(Double.class)) {
                    builder.addChannel(sym);
                    channels.add((Pair) Pair.of(sym, scores.getChannel(sym)));
                }
            }
            for (ScoredId id: results) {
                ScoredIdBuilder copy = ScoredIds.copyBuilder(id);
                for (Pair<Symbol,SparseVector> pair: cvs) {
                    if (pair.getRight().containsKey(id.getId())) {
                        copy.addChannel(pair.getLeft(), pair.getRight().get(id.getId()));
                    }
                }
                for (Pair<TypedSymbol<?>, Long2ObjectMap<?>> pair: channels) {
                    if (pair.getRight().containsKey(id.getId())) {
                        copy.addChannel((TypedSymbol) pair.getLeft(), pair.getRight().get(id.getId()));
                    }
                }
                builder.add(copy.build());
            }
            return builder.finish();
        } else {
            return results;
        }
    }

    /**
     * Get the default exclude set for a user.  The base implementation gets
     * all the items they have interacted with.
     *
     * @param user The user ID.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(long user) {
        return getDefaultExcludes(userEventDAO.getEventsForUser(user));
    }

    /**
     * Get the default exclude set for a user.  The base implementation returns
     * all the items they have interacted with (from {@link UserHistory#itemSet()}).
     *
     * @param user The user history.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(@Nullable UserHistory<? extends Event> user) {
        if (user == null) {
            return LongSets.EMPTY_SET;
        } else {
            return user.itemSet();
        }
    }

    /**
     * Determine the items for which predictions can be made for a certain user.
     * This implementation is naive and asks the DAO for all items; subclasses
     * should override it with something more efficient if practical.
     *
     * @param user The user's ID.
     * @return All items for which predictions can be generated for the user.
     */
    protected LongSet getPredictableItems(long user) {
        return itemDAO.getItemIds();
    }
}
