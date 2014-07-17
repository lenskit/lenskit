/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
 * Configuration loading support.
 * <p>
 * This package provides support for loading {@linkplain
 * org.grouplens.lenskit.core.LenskitConfiguration LensKit configurations} from configuration files
 * written using a Groovy-based DSL.  For example, the following:
 * </p>
 * <pre>{@code
 * // configure the item scorer
 * bind ItemScorer to ItemItemScorer
 * // set up a baseline predictor
 * bind BaselinePredictor to ItemUserMeanPredictor
 * // use the baseline for normalizing user ratings
 * bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
 * // the default neighborhood size is 20, so the next line isn't technically needed
 * set NeighborhoodSize to 20
 * }</pre>
 * <p>
 * See {@link ConfigHelpers} for entry points to quickly load configurations, and
 * {@link ConfigurationLoader} for more control over the configuration load process.
 * </p>
 */
package org.grouplens.lenskit.config;
