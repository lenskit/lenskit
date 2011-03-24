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

import com.google.inject.ImplementedBy;
import com.google.inject.throwingproviders.CheckedProvider;

/**
 * Provider of recommender services.
 *
 * <p>This provider allows client code to access recommender services.  If the
 * recommender is not available for some reason, the provider will throw an
 * exception.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ImplementedBy(SimpleRecommenderServiceProvider.class)
public interface RecommenderServiceProvider extends CheckedProvider<RecommenderService> {
    /**
     * Get (or build) the recommender service.
     * @return A recommender service. This can be a new object, an object from
     * a cache, or a recommender from somewhere else.  The returned object can
     * be freely used without locking; if the particular implementation is not
     * thread-safe or is otherwise scope-limited, it is the responsibility of the
     * provider and/or bindings to take care of that and return a usable object.
     * @throws RecommenderNotAvailableException if the recommender cannot be
     * retrieved or built.
     */
    RecommenderService get() throws RecommenderNotAvailableException;
}
