/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.transform.normalize;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Normalizes an item's vector.
 */
@DefaultImplementation(DefaultItemVectorNormalizer.class)
public interface ItemVectorNormalizer {
    /**
     * Normalize a vector with respect to an item vector.
     *
     * @param itemId   The item id to normalize a vector for.
     * @param vector The item's vector for reference.
     * @param target The vector to normalize. If {@code null}, the item vector is normalized.
     * @return The {@code target} vector, if specified. Otherwise, a fresh mutable vector
     *         containing a normalized copy of the item vector is returned.
     */
    MutableSparseVector normalize(long itemId, @Nonnull SparseVector vector,
                                  @Nullable MutableSparseVector target);

    /**
     * Make a vector transformation for an item. The resulting transformation will be applied
     * to item vectors to normalize and denormalize them.
     *
     * @param itemId   The item id to normalize for.
     * @param vector The item's vector to use as the reference vector.
     * @return The vector transformaition normalizing for this item.
     */
    VectorTransformation makeTransformation(long itemId, SparseVector vector);
}
