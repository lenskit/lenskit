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
package org.grouplens.lenskit.knn.item;

import javax.annotation.Nonnull;

import com.google.inject.ImplementedBy;

/**
 * Factory for creating new item-item recommender engines from models.
 *
 * Implementations of this interface take an {@link ItemItemModel} and create
 * an {@link ItemItemRecommenderService} backed by it.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ImplementedBy(DefaultItemItemRecommenderEngineFactory.class)
public interface ItemItemRecommenderServiceFactory {
    /**
     * Create a new recommender engine.
     * @param model The model backing the engine.
     * @return The newly-constructed recommender engine.
     */
    @Nonnull
    ItemItemRecommenderService create(@Nonnull ItemItemModel model);
}
