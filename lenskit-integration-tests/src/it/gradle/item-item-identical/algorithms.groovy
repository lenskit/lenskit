/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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


import it.unimi.dsi.fastutil.longs.Long2DoubleMap
import org.grouplens.lenskit.transform.truncate.VectorTruncator
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ItemBiasModel
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.knn.item.ItemSimilarity
import org.lenskit.knn.item.ModelSize
import org.lenskit.knn.item.model.ItemItemModel
import org.lenskit.knn.item.model.NormalizingItemItemModelProvider
import org.lenskit.knn.item.model.StandardVectorTruncatorProvider
import org.lenskit.similarity.VectorSimilarity
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

import javax.inject.Inject

class NonSymmetricSimilarity implements ItemSimilarity {
    final VectorSimilarity delegate

    @Inject
    NonSymmetricSimilarity(VectorSimilarity dlg) {
        delegate = dlg
    }

    @Override
    double similarity(long i1, Long2DoubleMap v1, long i2, Long2DoubleMap v2) {
        return delegate.similarity(v1, v2)
    }

    @Override
    boolean isSparse() {
        return delegate.isSparse()
    }

    @Override
    boolean isSymmetric() {
        return false
    }
}

def common = {
    bind ItemScorer to ItemItemScorer
    set NeighborhoodSize to 20
    set ModelSize to 500
    bind UserVectorNormalizer to BiasUserVectorNormalizer
    within (UserVectorNormalizer) {
        bind BiasModel to ItemBiasModel
    }
    bind (BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
    at (RatingPredictor) {
        // turn off baselines - make sure everything produces the same recs
        bind (BaselineScorer, ItemScorer) to null
    }
}

algorithm("Standard") {
    include common
}
algorithm("NonSymmetric") {
    include common
    bind ItemSimilarity to NonSymmetricSimilarity
}
algorithm("Normalizing") {
    include common
    bind ItemItemModel toProvider NormalizingItemItemModelProvider
    at (ItemItemModel) {
        bind VectorTruncator toProvider StandardVectorTruncatorProvider
    }
}
