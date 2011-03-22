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

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderService;
import org.grouplens.lenskit.RecommenderServiceProvider;
import org.grouplens.lenskit.data.RatingDataSource;
import org.grouplens.lenskit.params.BaselinePredictor;

import com.google.inject.Singleton;
import com.google.inject.throwingproviders.CheckedProvides;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ParallelItemRecommenderModule extends ItemRecommenderModule {

    @Override
    @CheckedProvides(RecommenderServiceProvider.class)
    @Singleton
    public RecommenderService provideRecommenderService(ItemItemRecommenderBuilder builder,
            RatingDataSource data, @BaselinePredictor RatingPredictor baseline) {
        return builder.build(data, baseline);
    }
}
