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
/**
 *
 */
package org.grouplens.lenskit.testing;

import java.util.Collection;
import java.util.Collections;

import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Base class providing facilities for doing tests against recommender Guice
 * modules.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class RecommenderModuleTest {
    /**
     * Method to get the current recommender module under test.
     * @return The module to be used, in conjunction with an empty rating set,
     * for testing.
     */
    protected abstract Module getModule();

    protected <T> T inject(Key<T> key) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            protected void configure() {
            }
            @SuppressWarnings("unused")
            @Provides public RatingDataAccessObject provideDataSource() {
                Collection<Rating> ratings = Collections.emptyList();
                return new RatingCollectionDAO(ratings);
            }
        }, getModule());
        return injector.getInstance(key);
    }
}
