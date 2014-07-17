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
package org.grouplens.lenskit.knn.item.model;

import com.google.common.base.Objects;
import org.grouplens.lenskit.knn.item.ItemSimilarityThreshold;
import org.grouplens.lenskit.knn.item.ModelSize;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.transform.truncate.ThresholdTruncator;
import org.grouplens.lenskit.transform.truncate.TopNTruncator;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Configure a vector truncator using the standard item-item model build logic.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StandardVectorTruncatorProvider implements Provider<VectorTruncator> {
    private final Threshold threshold;
    private final int modelSize;

    /**
     * Construct a new vector truncator provider.
     * @param thresh A threshold for filtering item similarities.
     * @param msize The maximum number of neighbors to retain for each item.
     */
    @Inject
    public StandardVectorTruncatorProvider(@ItemSimilarityThreshold Threshold thresh,
                                           @ModelSize int msize) {
        threshold = thresh;
        modelSize = msize;
    }

    @Override
    public VectorTruncator get() {
        if (modelSize > 0) {
            return new TopNTruncator(modelSize, threshold);
        } else {
            return new ThresholdTruncator(threshold);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(StandardVectorTruncatorProvider.class)
                      .add("modelSize", modelSize)
                      .add("threshold", threshold)
                      .toString();
    }
}
