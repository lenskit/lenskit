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
package org.grouplens.lenskit.eval.metrics.topn;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.item.model.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Metric that measures how diverse the items in the TopN list are.
 * 
 * To use this metric ensure that you have a reasonable item item model configured in each algorithm
 * The "default" will be used for generating similarity scores for use in this evaluation.
 * Your model must not use truncation of any sort (size of similarity value).
 * 
 * Example configuration:
 * <pre>
 *     bind VectorSimilarity to CosineVectorSimilarity
 *     bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
 *     within (UserVectorNormalizer) {
 *         bind (BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
 *         set MeanDamping to 5.0d
 *     }
 *     set ModelSize to 0
 *     bind (ItemSimilarityThreshold, Threshold) to NoThreshold
 *     root (ItemItemBuildContext)
 *     root (ItemSimilarity)
 * </pre>
 * 
 * I also recommend enabling model sharing and cacheing between algorithms to make this much more efficient.
 * 
 * This computes the average disimilarity (-1 * similarity) of all pairs of items. 
 * 
 * The number is large for non-diverse lists, and small for diverse lists.
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNDiversityMetric extends AbstractTestUserMetric {
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    public TopNDiversityMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        columns = ImmutableList.of(lbl);
    }
    
    @Override
    public Accum makeAccumulator(Attributed algo, TTDataSet ds) {
        return new Accum();
    }

    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return columns;
    }

    class Accum implements TestUserMetricAccumulator {
        double total = 0;
        int nusers = 0;
        
        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            
            List<ScoredId> recs;
            recs = user.getRecommendations(listSize, candidates, exclude);
            if (recs == null || recs.isEmpty()) {
                return userRow();
            } 
            
            double simSum = 0;

            LenskitRecommender rec = (LenskitRecommender) user.getRecommender();
            ItemSimilarityMetric metric = rec.get(ItemSimilarityMetric.class);
            ItemItemBuildContext context = metric.getContext();
            ItemSimilarity sim = metric.getSim();
            if (context == null || sim == null) {
                throw new RuntimeException("TopNDiversityMetric requires an build context and similarity function.");
            }
            
            for (ScoredId s1 : recs) {
                long i1 = s1.getId();
                SparseVector v1 = context.itemVector(i1);
                for (ScoredId s2 : recs) {
                    long i2 = s2.getId();
                    if(i1 == i2) {
                        continue;
                    }
                    SparseVector v2 = context.itemVector(i2);
                    simSum -= sim.similarity(i1,v1,i2,v2);
                }
            }
            
            int n = recs.size();
            simSum /= (n*n - n);
            
            total += simSum;
            nusers += 1;
            return userRow(simSum);
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (nusers > 0) {
                return finalRow(total / nusers);
            } else {
                return finalRow();
            }
        }
    }

    /**
     * Build a Top-N length metric to measure Top-N lists.
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNDiversityMetric> {
        private String label = "TopN.diversity";

        /**
         * Get the column label for this metric.
         * @return The column label.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Set the column label for this metric.
         * @param l The column label
         * @return The builder (for chaining).
         */
        public Builder setLabel(String l) {
            Preconditions.checkNotNull(l, "label cannot be null");
            label = l;
            return this;
        }

        @Override
        public TopNDiversityMetric build() {
            return new TopNDiversityMetric(label, listSize, candidates, exclude);
        }
    }

}
