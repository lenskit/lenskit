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
package org.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.results.Results;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An {@link ItemScorer} that implements a weighted Slope One algorithm.
 */
public class WeightedSlopeOneItemScorer extends SlopeOneItemScorer {
    @Inject
    public WeightedSlopeOneItemScorer(RatingVectorPDAO dao, SlopeOneModel model,
                                      @Nullable PreferenceDomain dom) {
        super(dao, model, dom);
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap ratings = dao.userRatingVector(user);

        List<Result> results = new ArrayList<>();
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long predicteeItem = iter.nextLong();
            if (!ratings.containsKey(predicteeItem)) {
                double total = 0;
                int nitems = 0;
                for (Long2DoubleMap.Entry e: Vectors.fastEntries(ratings)) {
                    long currentItem = e.getLongKey();
                    double currentDev = model.getDeviation(predicteeItem, currentItem);
                    if (!Double.isNaN(currentDev)) {
                        int weight = model.getCoratings(predicteeItem, currentItem);
                        total += (currentDev + e.getDoubleValue()) * weight;
                        nitems += weight;
                    }
                }
                if (nitems != 0) {
                    double predValue = total / nitems;
                    if (domain != null) {
                        predValue = domain.clampValue(predValue);
                    }
                    results.add(Results.create(predicteeItem, predValue));
                }
            }
        }
        return Results.newResultMap(results);
    }
}
