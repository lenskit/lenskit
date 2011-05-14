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

import static java.lang.Math.abs;
import static java.lang.Math.min;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.lenskit.AbstractDynamicRatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemRatingPredictor extends AbstractDynamicRatingPredictor {
    protected final ItemItemModel model;
    private final int neighborhoodSize;
    
    public ItemItemRatingPredictor(RatingDataAccessObject dao, ItemItemModel model, 
                                   @NeighborhoodSize int nnbrs,
                                   @Nullable UserRatingVectorNormalizer normalizer) {
        super(dao);
        this.model = model;
        neighborhoodSize = nnbrs;
    }
    
    public ItemItemModel getModel() {
        return model;
    }
    
    public LongSet getPredictableItems(long user, SparseVector ratings) {
        if (model.getBaselinePredictor() != null) {
            return model.getItemUniverse();
        } else {
            LongSet items = new LongOpenHashSet();
            LongIterator iter = ratings.keySet().iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                for (IndexedItemScore n: model.getNeighbors(item)) {
                    items.add(model.getItem(n.getIndex()));
                }
            }
            return items;
        }
    }
    
    private static final Comparator<IndexedItemScore> itemComp = new Comparator<IndexedItemScore>() {
        public int compare(IndexedItemScore s1, IndexedItemScore s2) {
            return Double.compare(s2.getScore(), s1.getScore());
        }
    };

    @Override
    public SparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
        VectorTransformation norm = model.normalizingTransformation(user, ratings);
        MutableSparseVector normed = ratings.mutableCopy();
        norm.apply(normed);

        LongSortedSet iset;
        if (items instanceof LongSortedSet)
            iset = (LongSortedSet) items;
        else
            iset = new LongSortedArraySet(items);
        
        MutableSparseVector preds = new MutableSparseVector(iset, Double.NaN);
        
        // for each item, compute its prediction
        LongIterator iter = iset.iterator();
        List<IndexedItemScore> scores = new ArrayList<IndexedItemScore>();
        LongList unpredItems = new LongArrayList();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            
            // find all potential neighbors
            for (IndexedItemScore score: model.getNeighbors(item)) {
                long oi = model.getItem(score.getIndex());
                if (normed.containsKey(oi))
                    scores.add(score);
            }
            Collections.sort(scores, itemComp);
            
            // accumulate prediction
            final int nnbrs = min(neighborhoodSize, scores.size());
            double sum = 0;
            double weight = 0;
            for (IndexedItemScore s: scores.subList(0, nnbrs)) {
                long oi = model.getItem(s.getIndex());
                double sim = s.getScore();
                weight += abs(sim);
                sum += sim * normed.get(oi);
            }
            if (weight > 0)
                preds.set(item, sum / weight);
            else
                unpredItems.add(item);
        }

        // denormalize the predictions
        norm.unapply(preds);
        
        // apply the baseline if applicable
        final BaselinePredictor baseline = model.getBaselinePredictor();
        if (baseline != null) {
            SparseVector basePreds = baseline.predict(user, ratings, unpredItems);
            for (Long2DoubleMap.Entry e: basePreds.fast()) {
                assert Double.isNaN(preds.get(e.getLongKey()));
                preds.set(e.getLongKey(), e.getDoubleValue());
            }
            return preds;
        } else {
            return preds.copy();
        }
    }

}
