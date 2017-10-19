/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/**
 * Configuration loading support.
 * <p>
 * This package provides support for loading {@linkplain
 * org.lenskit.LenskitConfiguration LensKit configurations} from configuration files
 * written using a Groovy-based DSL.  For example, the following:
 * </p>
 * <pre>{@code
 * // configure the item scorer
 * bind ItemScorer to ItemItemScorer
 * // set up a baseline scorer
 * bind (BaselineScorer, ItemScorer) to ItemMeanRatingPredictor
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
package org.lenskit.config;
