/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.matrix;

import it.unimi.dsi.fastutil.longs.LongCollection;

import org.grouplens.lenskit.params.meta.DefaultClass;

/**
 * Factory class for creating new matrix accumulators.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@DefaultClass(TruncatingSimilarityMatrixAccumulator.Factory.class)
public interface SimilarityMatrixAccumulatorFactory {
    
    /**
     * Construct a new matrix accumulator to accumulate similarities for the
     * specified entities.
     * 
     * @review Do we want to pass entities into this? Do we even want to use a
     *         factory, or directly inject accumulators or accumulator providers
     *         that use the DAO to get the items.
     * 
     * @param entities The items or users whose neighborhoods are to be
     *            collected.
     * @return A new similarity matrix accumulator.
     */
    public SimilarityMatrixAccumulator create(LongCollection entities);
}
