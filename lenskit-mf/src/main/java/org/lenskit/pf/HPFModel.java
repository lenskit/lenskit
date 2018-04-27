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
package org.lenskit.pf;

import org.apache.commons.math3.linear.RealMatrix;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.mf.MFModel;
import org.lenskit.util.keys.KeyIndex;

/**
 * The Poisson Factorization Model. This extends the SVD model.
 *
 * Poisson Factorization models each item as a vector of K latent attributes,
 * and each user as a vector of K latent preferences.
 * Then each rating is modeled as a Poisson distribution,
 * and the rate (mean) of the Poisson distribution is the inner product of
 * corresponding item latent vector and user latent vector.
 */
@DefaultProvider(HPFModelParallelProvider.class)
@Shareable
public final class HPFModel extends MFModel {
    private static final long serialVersionUID = 4L;

    /**
     * Construct a Poisson Factorization Model.
     * @param umat The user feature matrix (users x features).
     * @param imat The item feature matrix (items x features).
     * @param uidx The user index mapping
     * @param iidx The item index mapping
     */
    public HPFModel(RealMatrix umat, RealMatrix imat,
                    KeyIndex uidx, KeyIndex iidx) {
        super(umat, imat, uidx, iidx);
    }
}
