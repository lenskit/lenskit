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
package org.grouplens.lenskit.baseline;

import java.io.Serializable;
import java.util.Collection;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Serializer;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * BaselinePredictor is a subtype of RatingPredictor that guarantees 100%
 * coverage of items. It does not add any new methods to the RatingPredictor
 * interface. Because BaselinePredictors are often part of a "model" used by a
 * recommender algorithm, all BaselinePredictors are required to be Serializable
 * so that they can be easily written to or read from a file with a
 * {@link Serializer}.
 * 
 * @author Michael Ludwig
 */
public interface BaselinePredictor extends RatingPredictor, Serializable {
    /**
     * Refine predict to return mutable sparse vectors.
     * @see RatingPredictor#predict(long, SparseVector, Collection)
     */
    MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items);
}
