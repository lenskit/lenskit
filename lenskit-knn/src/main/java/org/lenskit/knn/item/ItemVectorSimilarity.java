/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.inject.Shareable;
import org.lenskit.similarity.VectorSimilarity;
import org.lenskit.util.parallel.MaybeThreadSafe;
import org.lenskit.util.reflect.ClassQueries;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Implementation of {@link ItemSimilarity} that delegates to a vector similarity.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class ItemVectorSimilarity implements ItemSimilarity, Serializable, MaybeThreadSafe {
    private static final long serialVersionUID = 1L;

    private VectorSimilarity delegate;

    @Inject
    public ItemVectorSimilarity(VectorSimilarity sim) {
        delegate = sim;
    }

    @Override
    public double similarity(long i1, Long2DoubleMap v1, long i2, Long2DoubleMap v2) {
        return delegate.similarity(v1, v2);
    }

    @Override
    public boolean isSparse() {
        return delegate.isSparse();
    }

    @Override
    public boolean isSymmetric() {
        return delegate.isSymmetric();
    }

    @Override
    public boolean isThreadSafe() {
        return ClassQueries.isThreadSafe(delegate);
    }

    @Override
    public String toString() {
        return "{item similarity: " + delegate.toString() + "}";
    }
}
