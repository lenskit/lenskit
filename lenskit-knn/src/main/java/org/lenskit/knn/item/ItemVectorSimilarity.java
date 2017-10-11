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
