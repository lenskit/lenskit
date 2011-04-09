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

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.SimilarityMatrix;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.util.IndexedItemScore;

import com.google.inject.ProvidedBy;

/**
 * Encapsulation of the predictor needed for item-item collaborative filtering.
 *
 * This class is used by {@link ItemItemRecommenderService} to do actual item-item
 * recommendation.  It encapsulates the various data accesses needed to support
 * item-item CF.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
@ProvidedBy(ItemItemModelProvider.class)
public class ItemItemModel implements Serializable {

    private static final long serialVersionUID = 7040201805529926395L;

    private final Index itemIndexer;
    private final SimilarityMatrix matrix;
    private final UserRatingVectorNormalizer normalizer;
    private final LongSortedSet itemUniverse;

    public ItemItemModel(Index indexer, UserRatingVectorNormalizer norm,
            SimilarityMatrix matrix, LongSortedSet items) {
        this.itemIndexer = indexer;
        this.normalizer = norm;
        this.matrix = matrix;
        this.itemUniverse = items;
    }

    public Iterable<IndexedItemScore> getNeighbors(long item) {
        int idx = itemIndexer.getIndex(item);
        if (idx >= 0) {
            return matrix.getNeighbors(idx);
        } else {
            return new ObjectCollections.EmptyCollection<IndexedItemScore>() {};
        }
    }

    public int getItemIndex(long id) {
        return itemIndexer.getIndex(id);
    }

    public long getItem(int idx) {
        return itemIndexer.getId(idx);
    }

    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }
    
    public VectorTransformation normalizingTransformation(long uid, SparseVector ratings) {
        return normalizer.makeTransformation(uid, ratings);
    }
}
