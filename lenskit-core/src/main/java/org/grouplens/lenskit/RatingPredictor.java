/*
 * LensKit, a reference implementation of recommender algorithms. Copyright
 * 2010-2011 Regents of the University of Minnesota This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Interface for rating prediction.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface RatingPredictor {
    /**
     * Predict the user's preference for a single item.
     * 
     * @param user The user ID.
     * @param item The item ID.
     * @return The preference, or {@link Double#NaN} if no preference can be
     *         predicted.
     */
    public double predict(long user, long item);

    /**
     * Generate predictions for a collection of items.
     * 
     * @param user the user ID
     * @param items the items for which predictions are desired
     * @return A mapping from item IDs to predicted preference. This mapping may
     *         not contain all requested items.
     */
    @Nonnull
    public SparseVector predict(long user, Collection<Long> items);
}
