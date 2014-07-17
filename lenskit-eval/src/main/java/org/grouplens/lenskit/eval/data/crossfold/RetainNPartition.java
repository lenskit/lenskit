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

import static java.lang.Math.min;

import java.util.List;

/**
 * Partition the event list by retaining a fixed number of elements.
 *
 * @param <E>
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RetainNPartition<E> implements PartitionAlgorithm<E> {

    final private int count;

    /**
     * Create a count partitioner.
     *
     * @param n The number of items to put in the train partition.
     */
    public RetainNPartition(int n) {
        count = n;
    }

    @Override
    public int partition(List<E> data) {
        return min(count, data.size());
    }

}
