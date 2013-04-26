/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.knn.item.model.NormalizingItemItemModelBuilder;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.DefaultItemVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.ItemVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.truncate.NoOpTruncator;
import org.grouplens.lenskit.transform.truncate.VectorTruncator;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.SimilarityDamping;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;

public class TestNormalizingItemItemAccuracy extends TestItemItemAccuracy {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitRecommenderEngineFactory factory) {
        factory.bind(ItemItemModel.class)
                .toProvider(NormalizingItemItemModelBuilder.class);
        factory.bind(ItemScorer.class)
                .to(ItemItemScorer.class);
        factory.bind(BaselinePredictor.class)
                .to(ItemUserMeanPredictor.class);
        factory.bind(UserVectorNormalizer.class)
                .to(BaselineSubtractingUserVectorNormalizer.class);
        factory.bind(ItemVectorNormalizer.class)
                .to(DefaultItemVectorNormalizer.class);
        factory.bind(VectorTruncator.class)
                .to(NoOpTruncator.class);
        factory.in(ItemSimilarity.class)
                .bind(VectorSimilarity.class)
                .to(CosineVectorSimilarity.class);
        factory.in(ItemSimilarity.class)
                .set(SimilarityDamping.class)
                .to(100.0);
        factory.set(NeighborhoodSize.class).to(30);
    }
}
