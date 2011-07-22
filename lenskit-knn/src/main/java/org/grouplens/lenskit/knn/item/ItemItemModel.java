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

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectCollections;

import java.io.Serializable;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrix;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;
import org.grouplens.lenskit.util.IndexedItemScore;

@Built
@DefaultBuilder(ItemItemModelBuilder.class)
public class ItemItemModel implements Serializable {
    private static final long serialVersionUID = -5986236982760043379L;
    
    private final Index itemIndexer;
    private final SimilarityMatrix matrix;
    private final VectorNormalizer<? super UserRatingVector> normalizer;
    private final BaselinePredictor baseline;
    private final LongSortedSet itemUniverse;
    
    public ItemItemModel(Index indexer, SimilarityMatrix matrix,
                         VectorNormalizer<? super UserRatingVector> norm, BaselinePredictor baseline,
                         LongSortedSet items) {
        this.itemIndexer = indexer;
        this.normalizer = norm;
        this.baseline = baseline;
        this.matrix = matrix;
        this.itemUniverse = items;
    }
    
    public VectorNormalizer<? super UserRatingVector> getNormalizer() {
        return normalizer;
    }
    
    public BaselinePredictor getBaselinePredictor() {
        return baseline;
    }
    
    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }
    
    public SimilarityMatrix getSimilarityMatrix() {
        return matrix;
    }
    
    public int getItemIndex(long id) {
        return itemIndexer.getIndex(id);
    }

    public long getItem(int idx) {
        return itemIndexer.getId(idx);
    }
    
    public Iterable<IndexedItemScore> getNeighbors(long item) {
        int idx = itemIndexer.getIndex(item);
        if (idx >= 0) {
            return matrix.getNeighbors(idx);
        } else {
            return new ObjectCollections.EmptyCollection<IndexedItemScore>() {};
        }
    }
    
    public VectorTransformation normalizingTransformation(UserRatingVector user) {
        return normalizer.makeTransformation(user);
    }
}
