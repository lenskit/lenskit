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
 * Normalize a user's vector. This vector is typically a rating or purchase vector.
 * <p>
 * This interface is essentially a user-aware version of {@link VectorNormalizer}. The
 * default implementation, {@link DefaultUserVectorNormalizer}, delegates to a
 * {@link VectorNormalizer}. Implement this interface directly to create a normalizer
 * that is aware of the fact that it is normalizing a user and e.g. uses user properties
 * outside the vector to aid in the normalization. Otherwise, use a context-sensitive
 * binding of {@link VectorNormalizer} to configure the user vector normalizer:
 * </p>
 *
 * {@code
 * factory.in(UserVectorNormalizer.class)
 * .bind(VectorNormalizer.class)
 * .to(MeanVarianceNormalizer.class);
 * }
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see VectorNormalizer
 * @since 0.11
 */
@DefaultImplementation(DefaultUserVectorNormalizer.class)
public interface UserVectorNormalizer {
    /**
     * Make a vector transformation for a user. The resulting transformation will be applied
     * to user vectors to normalize and denormalize them.
     *
     * @param user   The user ID to normalize for.
     * @param vector The user's vector to use as the reference vector.
     * @return The vector transformation normalizing for this user.
     */
    InvertibleFunction<Long2DoubleMap,Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector);
}
