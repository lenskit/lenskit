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
package org.lenskit.eval.crossfold;

import org.grouplens.lenskit.data.event.Rating;

public final class SplitMethods {
    private SplitMethods() {}

    /**
     * Create a crossfold method that splits users into disjoint partitions.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @return The crossfold method.
     */
    public static SplitMethod partitionUsers(Order<Rating> order, PartitionAlgorithm<Rating> part) {
        return new UserPartitionSplitMethod(order, part);
    }

    /**
     * Create a crossfold method that splits users into disjoint samples.
     * @param order The ordering for user rating partitions.
     * @param part the partition algorithm for user ratings.
     * @param size The number of users per sample.
     * @return The crossfold method.
     */
    public static SplitMethod sampleUsers(Order<Rating> order, PartitionAlgorithm<Rating> part, int size) {
        return new UserSampleSplitMethod(order, part, size);
    }

    /**
     * Create a crossfold method that partitions ratings into disjoint partitions.
     * @return The crossfold method.
     */
    public static SplitMethod partitionRatings() {
        return new RatingPartitionSplitMethod();
    }
}
