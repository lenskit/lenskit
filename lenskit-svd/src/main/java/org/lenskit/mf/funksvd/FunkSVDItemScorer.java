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
package org.lenskit.mf.funksvd;

import org.lenskit.bias.BiasModel;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.mf.svd.BiasedMFItemScorer;
import org.lenskit.mf.svd.DomainClampingKernel;
import org.lenskit.mf.svd.DotProductKernel;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Do recommendations and predictions based on SVD matrix factorization.  This is simply a convenience class to make
 * it easy to get a FunkSVD scorer; it specializes the biased MF scorer to require a FunkSVD model.
 */
public class FunkSVDItemScorer extends BiasedMFItemScorer {
    /**
     * Construct the item scorer.
     *
     * @param model    The model.
     * @param baseline The baseline scorer.  Be very careful when configuring a different baseline
     *                 at runtime than at model-build time; such a configuration is unlikely to
     *                 perform well.
     * @param dom      The preference domain.
     */
    @Inject
    public FunkSVDItemScorer(FunkSVDModel model, BiasModel baseline,
                             @Nullable PreferenceDomain dom) {
        super(model,
              dom == null ? new DotProductKernel() : new DomainClampingKernel(dom),
              baseline);
    }

    @Override
    public FunkSVDModel getModel() {
        return (FunkSVDModel) super.getModel();
    }
}
