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
package org.grouplens.lenskit.mf.svd;

import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.core.Shareable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * Kernel function that uses the dot product of the user and item vectors.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@Immutable
public class DotProductKernel implements BiasedMFKernel, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public double apply(double bias, @Nonnull RealVector user, @Nonnull RealVector item) {
        return bias + user.dotProduct(item);
    }

    @Override
    public int hashCode() {
        return DotProductKernel.class.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass().equals(getClass());
    }

    @Override
    public String toString() {
        return "DotProductKernel()";
    }
}
