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
package org.grouplens.lenskit;

import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * <p>
 * Builders are used to construct expensive objects that depend on a
 * {@link RatingBuildContext}. Once built the objects do not depend on the
 * context or {@link RatingDataAccessObject dao}. The expected behavior of a
 * RecommenderComponentBuilder is that it only ever builds an object once per context. This way, the
 * same builder can be used to share instances during a build.
 * </p>
 * <p>
 * An example of an object a RecommenderComponentBuilder would produce is an item-item similarity
 * matrix used in item-item recommenders.
 * </p>
 * 
 * @author Michael Ludwig
 * @param <M>
 */
public interface RecommenderComponentBuilder<M> {
    /**
     * Build the model object of type M within the given build context. If the
     * it has already been built for the given context, the old instance should
     * be returned, making the builder memoize the constructed objects. Hint:
     * this can be easily done by extending {@link AbstractRecommenderComponentBuilder}.
     * 
     * @param context The context to build in
     * @return An instance of type M built from the context
     */
    public M build(RatingBuildContext context);
}
