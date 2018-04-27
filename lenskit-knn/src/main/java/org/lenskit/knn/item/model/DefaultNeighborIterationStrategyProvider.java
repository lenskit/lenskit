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
package org.lenskit.knn.item.model;

import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider to auto-select an appropriate neighbor iteration strategy.  It uses the similarity
 * function to decide which to use, using {@link SparseNeighborIterationStrategy} if the function
 * is sparse and {@link BasicNeighborIterationStrategy} otherwise.
 *
 * @see ItemSimilarity#isSparse()
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
