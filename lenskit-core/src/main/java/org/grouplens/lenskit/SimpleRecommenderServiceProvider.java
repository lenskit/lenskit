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

import javax.annotation.Nonnull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * {@link RecommenderServiceProvider} that returns the recommender service
 * injected into it.  It serves as the default implementation of
 * {@link RecommenderServiceProvider}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Singleton
public class SimpleRecommenderServiceProvider implements RecommenderServiceProvider {
    private final @Nonnull RecommenderService service;

    @Inject
    public SimpleRecommenderServiceProvider(@Nonnull RecommenderService service) {
        this.service = service;
    }

    /**
     * Get the recommender service.  If the recommender needs to be built, it
     * will block all other threads asking for recommenders.
     */
    @Override
    public @Nonnull RecommenderService get() {
        return service;
    }

}
