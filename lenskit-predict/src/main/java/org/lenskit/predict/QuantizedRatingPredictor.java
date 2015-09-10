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
package org.lenskit.predict;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractRatingPredictor;
import org.lenskit.basic.PredictionScorer;
import org.lenskit.results.Results;
import org.lenskit.transform.quantize.Quantizer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A rating predictor wrapper that quantizes scores to compute predictions.
 */
public class QuantizedRatingPredictor extends AbstractRatingPredictor {
    private final ItemScorer itemScorer;
    private final Quantizer quantizer;

    /**
     * Construct a new quantized predictor.
     * @param scorer The item scorer to use.
     * @param q The quantizer.
     */
    @Inject
    public QuantizedRatingPredictor(@PredictionScorer ItemScorer scorer,
                                    Quantizer q) {
        itemScorer = scorer;
        quantizer = q;
    }

    private void quantize(MutableSparseVector scores) {
        for (VectorEntry e: scores) {
            scores.set(e, quantizer.getIndexValue(quantizer.index(e.getValue())));
        }
    }

    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        ResultMap scores = itemScorer.scoreWithDetails(user, items);
        List<Result> results = new ArrayList<>();
        for (Result raw: scores) {
            int idx = quantizer.index(raw.getScore());
            double score = quantizer.getIndexValue(idx);
            results.add(Results.rescore(raw, score));
        }
        return Results.newResultMap(results);
    }
}
