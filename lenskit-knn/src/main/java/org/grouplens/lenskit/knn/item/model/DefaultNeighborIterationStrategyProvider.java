/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.knn.item.ItemSimilarity;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider to auto-select an appropriate neighbor iteration strategy.  It uses the similarity
 * function to decide which to use, using {@link SparseNeighborIterationStrategy} if the function
 * is sparse and {@link BasicNeighborIterationStrategy} otherwise.
 *
 * @see org.grouplens.lenskit.knn.item.ItemSimilarity#isSparse()
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DefaultNeighborIterationStrategyProvider implements Provider<NeighborIterationStrategy> {
    private final ItemSimilarity similarity;

    /**
     * Construct a new provider.
     * @param sim The item similarity function to use.
     */
    @Inject
    public DefaultNeighborIterationStrategyProvider(@Transient ItemSimilarity sim) {
        similarity = sim;
    }

    @Override
    public NeighborIterationStrategy get() {
        if (similarity.isSparse()) {
            return new SparseNeighborIterationStrategy();
        } else {
            return new BasicNeighborIterationStrategy();
        }
    }
}
