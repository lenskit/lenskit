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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.PrimaryScorer;
import org.grouplens.lenskit.baseline.ScoreSource;
import org.lenskit.data.ratings.PreferenceDomain;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Basic {@link org.grouplens.lenskit.RatingPredictor} backed by an
 * {@link org.grouplens.lenskit.ItemScorer}.  The scores are clamped to the preference domain
 * but otherwise unmodified.
 *
 * <p>If a baseline predictor is provided, then it is used to supply predictions that the item
 * scorer could not.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public final class SimpleRatingPredictor extends AbstractRatingPredictor {
    public static final TypedSymbol<ScoreSource> PREDICTION_SOURCE_SYMBOL =
            TypedSymbol.of(ScoreSource.class, "org.grouplens.lenskit.basic.PredictionSource");

    private final ItemScorer scorer;
    @Nullable
    private final ItemScorer baselineScorer;
    @Nullable
    private final PreferenceDomain preferenceDomain;

    @Inject
    public SimpleRatingPredictor(@PrimaryScorer ItemScorer scorer,
                                 @Nullable @BaselineScorer ItemScorer baseline,
                                 @Nullable PreferenceDomain domain) {
        this.scorer = scorer;
        baselineScorer = baseline;
        preferenceDomain = domain;
    }

    /**
     * Get the backing scorer.
     *
     * @return The item scorer.
     */
    public ItemScorer getScorer() {
        return scorer;
    }

    /**
     * Get the preference domain.
     * @return The preference domain
     */
    @Nullable
    public PreferenceDomain getPreferenceDomain() {
        return preferenceDomain;
    }

    /**
     * Get the baseline predictor.
     *
     * @return The baseline predictor, or {@code null} if no baseline is configured.
     */
    @Nullable
    public ItemScorer getBaselineScorer() {
        return baselineScorer;
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector scores) {
        scorer.score(user, scores);
        LongSet fallbackKeys = LongSets.EMPTY_SET;

        if (baselineScorer != null) {
            fallbackKeys = scores.unsetKeySet();
            MutableSparseVector unpred = MutableSparseVector.create(fallbackKeys);
            baselineScorer.score(user, unpred);
            scores.set(unpred);
        }

        // FIXME Make this faster
        Long2ObjectMap<ScoreSource> chan = scores.getOrAddChannel(PREDICTION_SOURCE_SYMBOL);
        for (VectorEntry e: scores) {
            long key = e.getKey();
            ScoreSource source = ScoreSource.PRIMARY;
            if (fallbackKeys.contains(key)) {
                source = ScoreSource.BASELINE;
            }
            chan.put(key, source);
        }

        if (preferenceDomain != null) {
            preferenceDomain.clampVector(scores);
        }
    }
}
