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
import org.lenskit.inject.Shareable;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Vector normlizer that subtracts the mean from every value.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class MeanCenteringVectorNormalizer extends AbstractVectorNormalizer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public InvertibleFunction<Long2DoubleMap, Long2DoubleMap> makeTransformation(Long2DoubleMap reference) {
        return new Transform(Vectors.mean(reference));
    }

    private static class Transform implements VectorTransformation {
        private final double mean;

        public Transform(double mean) {
            this.mean = mean;
        }

        @Nullable
        @Override
        public Long2DoubleMap apply(@Nullable Long2DoubleMap input) {
            if (input == null) {
                return null;
            }

            return Vectors.addScalar(input, -mean);
        }

        @Override
        public Long2DoubleMap unapply(Long2DoubleMap input) {
            if (input == null) {
                return null;
            }

            return Vectors.addScalar(input, mean);
        }


    }
}
