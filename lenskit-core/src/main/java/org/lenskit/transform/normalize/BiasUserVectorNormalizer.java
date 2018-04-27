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
import org.lenskit.bias.BiasModel;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;

/**
 * User vector normalizer that subtracts user-item biases.
 */
public class BiasUserVectorNormalizer extends AbstractUserVectorNormalizer {
    private final BiasModel model;

    /**
     * Construct a new normalizer.
     * @param bias The bias model to subtract from user vector values.
     */
    @Inject
    public BiasUserVectorNormalizer(BiasModel bias) {
        model = bias;
    }

    @Override
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(long user, Long2DoubleMap vector) {
        return new Transform(user);
    }

    private class Transform implements VectorTransformation {
        private final long user;
        private final double userBias;

        Transform(long uid) {
            user = uid;
            userBias = model.getIntercept() + model.getUserBias(user);
        }

        @Override
        public Long2DoubleMap unapply(Long2DoubleMap input) {
            Long2DoubleMap biases = model.getItemBiases(input.keySet());
            return Vectors.combine(input, biases, 1.0, userBias);
        }

        @Override
        public Long2DoubleMap apply(Long2DoubleMap input) {
            Long2DoubleMap biases = model.getItemBiases(input.keySet());
            return Vectors.combine(input, biases, -1.0, -userBias);
        }
    }
}
