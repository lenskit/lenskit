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
package org.lenskit.similarity;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.transform.quantize.Quantizer;
import org.lenskit.util.math.MutualInformationAccumulator;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Similarity function that assumes the two vectors are paired samples from 2
 * correlated random variables. Using this we estimate the mutual information
 * between the two variables.
 *
 * Note, this uses the naive estimator of mutual information, which can be
 * heavily biased when the two vectors have little overlap.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MutualInformationVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;

    private final Quantizer quantizer;

    /**
     * Construct a new mutual information similarity.
     *
     * @param quantizer A quantizer to allow discrete mutual information to be computed.
     */
    @Inject
    public MutualInformationVectorSimilarity(Quantizer quantizer) {
        this.quantizer = quantizer;
    }

    @Override
    public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
        MutualInformationAccumulator accum = new MutualInformationAccumulator(quantizer.getCount());

        for (Long2DoubleMap.Entry e: vec1.long2DoubleEntrySet()) {
            long k = e.getLongKey();
            if (vec2.containsKey(k)) {
                accum.count(quantizer.index(e.getDoubleValue()),
                            quantizer.index(vec2.get(k)));
            }
        }

        return accum.getMutualInformation();
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
