/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens;

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.vector.SparseVector;

/**
 * Interface for rating prediction.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@ParametersAreNonnullByDefault
public interface RatingPredictor {
	/**
	 * Generate a prediction for a single item.
	 * @param user the user ID
	 * @param ratings the user's rating history
	 * @param item the item for which a prediction is required
	 * @return the prediction, or <tt>null</tt> if no prediction is possible
	 */
	@Nullable @CheckForNull
	public ScoredId predict(long user, SparseVector ratings, long item);
	
	/**
	 * Generate predictions for a collection of items.
	 * @param user the user ID
	 * @param ratings the user's ratings
	 * @param items the items for which predictions are desired
	 * @return A mapping from item IDs to predicted preference.  This mapping
	 * may not contain all requested items.
	 */
	@Nonnull
	public SparseVector predict(long user, SparseVector ratings, Collection<Long> items);
}