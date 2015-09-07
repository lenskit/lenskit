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
package org.grouplens.lenskit.eval.data.crossfold;

import org.lenskit.data.ratings.Rating;
import org.lenskit.eval.crossfold.SortOrder;
import org.lenskit.eval.crossfold.HistoryPartitionMethod;

import java.util.List;
import java.util.Random;

/**
 * A train-test holdout method.
 */
public class Holdout {
    private final SortOrder order;
    private final HistoryPartitionMethod partitionMethod;

    public Holdout(SortOrder ord, HistoryPartitionMethod part) {
        order = ord;
        partitionMethod = part;
    }

    public SortOrder getOrder() {
        return order;
    }

    public HistoryPartitionMethod getPartitionMethod() {
        return partitionMethod;
    }

    public int partition(List<Rating> ratings, Random rng) {
        if (order == null || partitionMethod == null) {
            throw new IllegalStateException("Unconfigured holdout");
        }
        order.apply(ratings, rng);
        return partitionMethod.partition(ratings);
    }
}
