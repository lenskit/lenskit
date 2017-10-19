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
package org.lenskit.transform.normalize;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.InvertibleFunction;

/**
 * Normalize vectors by applying a reversible transformation with respect to
 * a reference vector.  The reference vector is used to compute the normalization,
 * and it is applied to the target vector; this allows e.g. the user's average
 * rating to be subtracted from a set of ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(IdentityVectorNormalizer.class)
public interface VectorNormalizer {

    /**
     * Create a vector transformation that normalizes and denormalizes vectors
     * with respect to a reference vector.  The reference vector is used to compute any data needed for the
     * normalization.  For example, a mean-centering normalization will subtract the mean of the reference vector
     * from any vector to which it is applied, and add back the reference mean when it is unapplied.
     *
     * <p>This allows transformations to be applied multiple times to different vectors and also unapplied.
     * <p>
     * If the reference vector is empty, the returned transformation should be
     * the identity transform. Results are undefined if the reference vector is
     * not complete or contains NaN values.
     * <p>
     * If the normalization needs to retain a copy of the sparse vector, it will
     * take an immutable copy.
     *
     * @param reference The reference vector.
     * @return A transformation built from the reference vector.
     */
    InvertibleFunction<Long2DoubleMap,Long2DoubleMap> makeTransformation(Long2DoubleMap reference);
}
