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

import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.model.ItemItemBuildContext;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 * of items and stores the results in a {@code DeviationMatrix} and
 * {@code CoratingMatrix}. These matrices are later used by a
 * {@code SlopeOneItemScorer}.
 */
public class SlopeOneModelProvider implements Provider<SlopeOneModel> {
    private final SlopeOneModelDataAccumulator accumulator;

    private final ItemItemBuildContext buildContext;

    @Inject
    public SlopeOneModelProvider(@Transient ItemItemBuildContext context,
                                 @DeviationDamping double damping) {

        buildContext = context;
        accumulator = new SlopeOneModelDataAccumulator(damping, context.getItems());
    }

    /**
     * Constructs and returns a {@link SlopeOneModel}.
     */
    @Override
    public SlopeOneModel get() {
        LongSet items = buildContext.getItems();
        LongIterator outer = items.iterator();
        while (outer.hasNext()) {
            final long item1 = outer.nextLong();
            final Long2DoubleSortedMap vec1 = buildContext.itemVector(item1);
            LongIterator inner = items.iterator();
            while (inner.hasNext()) {
                final long item2 = inner.nextLong();
                if (item1 != item2) {
                    Long2DoubleSortedMap vec2 = buildContext.itemVector(item2);
                    accumulator.putItemPair(item1, vec1, item2, vec2);
                }
            }
        }
        return new SlopeOneModel(accumulator.buildMatrix());
    }
}
