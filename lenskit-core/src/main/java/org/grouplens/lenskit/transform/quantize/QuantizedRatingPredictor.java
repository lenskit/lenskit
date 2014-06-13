/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.transform.quantize;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.PrimaryScorer;
import org.grouplens.lenskit.basic.AbstractRatingPredictor;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A rating predictor wrapper that quantizes predictions.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class QuantizedRatingPredictor extends AbstractRatingPredictor implements RatingPredictor {
    private final ItemScorer itemScorer;
    private final ItemScorer baselineScorer;
    private final Quantizer quantizer;

    /**
     * Construct a new quantized predictor.
     * @param scorer The item scorer to use.
     * @param baseline A baseline scorer to fall back to.
     * @param q The quantizer.
     */
    @Inject
    public QuantizedRatingPredictor(@PrimaryScorer ItemScorer scorer,
                                    @Nullable @BaselineScorer ItemScorer baseline,
                                    Quantizer q) {
        itemScorer = scorer;
        baselineScorer = baseline;
        quantizer = q;
    }

    private void quantize(MutableSparseVector scores) {
        for (VectorEntry e: scores.fast()) {
            scores.set(e, quantizer.getIndexValue(quantizer.index(e.getValue())));
        }
    }

    @Override
    public void predict(long user, @Nonnull MutableSparseVector scores) {
        itemScorer.score(user, scores);
        if (baselineScorer != null) {
            LongSet unset = scores.unsetKeySet();
            if (!unset.isEmpty()) {
                SparseVector bscores = baselineScorer.score(user, unset);
                scores.set(bscores);
            }
        }
        quantize(scores);
    }
}
