/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.mf.svd;

import mikera.vectorz.AVector;
import org.grouplens.grapht.annotation.DefaultImplementation;

import javax.annotation.Nonnull;

/**
 * A kernel for biased matrix factorization.  This function combines a user-item bias (baseline
 * score) and the user- and item-factor vectors to make a final score.
 *
 * <p>Note that not all kernels are compatible with all model build strategies.</p>
 *
 * <p>Kernels should be serializable and shareable.</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(DotProductKernel.class)
public interface BiasedMFKernel {
    /**
     * Apply the kernel function.
     *
     *
     * @param bias The combined user-item bias term (the baseline score, usually).
     * @param user The user-factor vector.
     * @param item The item-factor vector.
     * @return The kernel function value (combined score).
     * @throws IllegalArgumentException if the user and item vectors have different lengths.
     */
    double apply(double bias, @Nonnull AVector user, @Nonnull AVector item);
}
