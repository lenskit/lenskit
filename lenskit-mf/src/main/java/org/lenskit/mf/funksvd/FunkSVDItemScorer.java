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

import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.bias.BiasModel;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.mf.BiasedMFItemScorer;
import org.lenskit.mf.MFModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Do recommendations and predictions based on SVD matrix factorization.  This extends the {@link BiasedMFItemScorer}
 * to default to using a FunkSVD model, and to clamp predicted ratings to the range of valid ratings (if a preference
 * domain is configured).
 */
public class FunkSVDItemScorer extends BiasedMFItemScorer {
    private final PreferenceDomain domain;

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
    public FunkSVDItemScorer(@DefaultImplementation(FunkSVDModel.class) MFModel model,
                             BiasModel baseline,
                             @Nullable PreferenceDomain dom) {
        super(model, baseline);
        domain = dom;
    }

    @Override
    protected double computeScore(double bias, @Nonnull RealVector user, @Nonnull RealVector item) {
        if (domain == null) {
            return super.computeScore(bias, user, item);
        } else {
            double result = bias;
            int n = user.getDimension();
            for (int i = 0; i < n; i++) {
                result = domain.clampValue(result + user.getEntry(i) * item.getEntry(i));
            }
            return result;
        }
    }

    @Override
    public FunkSVDModel getModel() {
        return (FunkSVDModel) super.getModel();
    }
}
