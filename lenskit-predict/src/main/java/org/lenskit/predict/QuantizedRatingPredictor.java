/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.predict;

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
