/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.vector.ImmutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.SimilarityMatrix;
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulatorFactory;
import org.grouplens.lenskit.knn.item.params.ItemSimilarity;
import org.grouplens.lenskit.norm.NormalizedRatingSnapshot;
import org.grouplens.lenskit.params.Baseline;
import org.grouplens.lenskit.util.IntSortedArraySet;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.grouplens.lenskit.util.SymmetricBinaryFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemItemModelBuilder extends RecommenderComponentBuilder<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelBuilder.class);

    private Similarity<? super SparseVector> itemSimilarity;
    private SimilarityMatrixAccumulatorFactory matrixSimilarityFactory;
    
    private BaselinePredictor baseline;
    private NormalizedRatingSnapshot normalizedData;
    
    @ItemSimilarity
    public void setSimilarity(Similarity<? super SparseVector> similarity) {
        itemSimilarity = similarity;
    }
    
    public void setSimilarityMatrixAccumulatorFactory(SimilarityMatrixAccumulatorFactory factory) {
        matrixSimilarityFactory = factory;
    }
    
    @Baseline
    public void setBaselinePredictor(@Nullable BaselinePredictor predictor) {
        baseline = predictor;
    }

    public void setNormalizedRatingSnapshot(NormalizedRatingSnapshot data) {
        this.normalizedData = data;
    }
    
    @Override
    public ItemItemModel build() {
        ItemItemModelBuildStrategy similarityStrategy = createBuildStrategy(matrixSimilarityFactory, itemSimilarity);
        
        BuildState state = new BuildState(normalizedData, similarityStrategy.needsUserItemSets());

        SimilarityMatrix matrix = similarityStrategy.buildMatrix(state);
        LongSortedArraySet items = new LongSortedArraySet(state.itemIndex.getIds());
        
        return new ItemItemModel(state.itemIndex, matrix, normalizedData.getNormalizer(),
                                 baseline, items);
    }
    
    @ParametersAreNonnullByDefault
    final class BuildState {
        public final Index itemIndex;
        public ArrayList<SparseVector> itemRatings;
        public final @Nullable Long2ObjectMap<IntSortedSet> userItemSets;
        public final int itemCount;

        public BuildState(NormalizedRatingSnapshot data, boolean trackItemSets) {
            itemIndex = data.itemIndex();
            itemCount = itemIndex.getObjectCount();
            itemRatings = new ArrayList<SparseVector>();

            if (trackItemSets)
                userItemSets = new Long2ObjectOpenHashMap<IntSortedSet>(data.getUserIds().size());
            else
                userItemSets = null;

            logger.debug("Pre-processing ratings");
            buildItemRatings(data);
        }

        /**
         * Transpose the ratings matrix so we have a list of item
         * rating vectors.
         * @todo Fix this method to abstract item collection.
         * @todo Review and document this method.
         */
        private void buildItemRatings(NormalizedRatingSnapshot data) {
            final boolean collectItems = userItemSets != null;
            final int nitems = itemCount;

            // Create and initialize the transposed array to collect ratings
            ArrayList<Long2DoubleMap> workMatrix = new ArrayList<Long2DoubleMap>(nitems);
            for (int i = 0; i < nitems; i++) {
                workMatrix.add(new Long2DoubleOpenHashMap(20));
            }
            
            LongIterator userIter = data.getUserIds().iterator();
            while (userIter.hasNext()) {
                final long user = userIter.nextLong();
                Collection<IndexedRating> ratings = data.getUserRatings(user);
                
                final int nratings = ratings.size();
                // allocate the array ourselves to avoid an array copy
                int[] userItemArr = null;
                IntCollection userItems = null;
                if (collectItems) {
                    userItemArr = new int[nratings];
                    userItems = IntArrayList.wrap(userItemArr, 0);
                }
                
                for (IndexedRating rating: ratings) {
                    final int idx = rating.getItemIndex();
                    assert idx >= 0 && idx < nitems;
                    // get the item's rating vector
                    Long2DoubleMap ivect = workMatrix.get(idx);
                    ivect.put(user, (double) rating.getRating());
                    if (userItems != null)
                        userItems.add(idx);
                }
                if (collectItems) {
                    final IntSortedSet itemSet = new IntSortedArraySet(userItemArr, 0, userItems.size());
                    userItemSets.put(user, itemSet);
                }
            }

            // convert the temporary work matrix into a real matrix
            itemRatings = new ArrayList<SparseVector>(workMatrix.size());
            ListIterator<Long2DoubleMap> iter = workMatrix.listIterator();
            while (iter.hasNext()) {
                Long2DoubleMap ratings = iter.next();
                SparseVector v = new ImmutableSparseVector(ratings);
                assert v.size() == ratings.size();
                itemRatings.add(v);
                iter.set(null);                // clear the array so GC can free
            }
            assert itemRatings.size() == workMatrix.size();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ItemItemModelBuildStrategy createBuildStrategy(SimilarityMatrixAccumulatorFactory matrixFactory, 
                                                             Similarity<? super SparseVector> similarity) {
        if (similarity instanceof OptimizableVectorSimilarity) {
            if (similarity instanceof SymmetricBinaryFunction)
                return new SparseSymmetricModelBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
            else
                return new SparseModelBuildStrategy(matrixFactory,
                        (OptimizableVectorSimilarity) similarity);
        } else {
            if (similarity instanceof SymmetricBinaryFunction)
                return new SymmetricModelBuildStrategy(matrixFactory, similarity);
            else
                return new SimpleModelBuildStrategy(matrixFactory, similarity);
        }
    }
}
