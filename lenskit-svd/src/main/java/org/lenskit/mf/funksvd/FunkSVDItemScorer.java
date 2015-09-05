/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.mf.funksvd;

import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.mf.svd.BiasedMFItemScorer;
import org.lenskit.mf.svd.DomainClampingKernel;
import org.lenskit.mf.svd.DotProductKernel;
import org.lenskit.api.ItemScorer;
import org.lenskit.baseline.BaselineScorer;

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
    public FunkSVDItemScorer(FunkSVDModel model, @BaselineScorer ItemScorer baseline,
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
