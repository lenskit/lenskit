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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.lenskit.data.ratings.RatingSummary;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model that returns items' average ratings.  For an item \\(i\\), the global bias \\(b\\) plus the
 * item bias \\(b_i\\) will equal the item's average rating.  User biases are all zero.
 */
public class ItemAverageRatingBiasModelProvider implements Provider<ItemBiasModel> {
    private final RatingSummary summary;
    private final double damping;

    @Inject
    public ItemAverageRatingBiasModelProvider(RatingSummary rs, @BiasDamping double damp) {
        summary = rs;
        damping = damp;
    }

    @Override
    public ItemBiasModel get() {
        Long2DoubleMap offsets;

        if (damping > 0) {
            offsets = new Long2DoubleOpenHashMap();
            LongIterator iter = summary.getItems().iterator();
            while (iter.hasNext()) {
                long item = iter.nextLong();
                double off = summary.getItemOffset(item);
                int count = summary.getItemRatingCount(item);
                offsets.put(item, count * off / (count + damping));
            }
        } else {
            offsets = summary.getItemOffets();
        }

        return new ItemBiasModel(summary.getGlobalMean(), offsets);
    }
}
