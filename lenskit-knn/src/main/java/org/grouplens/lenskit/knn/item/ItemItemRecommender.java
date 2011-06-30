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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.PredictorBasedDRItemRecommender;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.UserRatingVector;
import org.grouplens.lenskit.util.IndexedItemScore;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemRecommender extends PredictorBasedDRItemRecommender {
    protected final ItemItemRatingPredictor predictor;
    
    /**
     * Construct a new recommender from a predictor.
     * @param predictor The predictor to use.
     */
    public ItemItemRecommender(RatingDataAccessObject dao, ItemItemRatingPredictor predictor) {
        super(dao, predictor);
        this.predictor = predictor;
    }
    
    @Override
    public LongSet getPredictableItems(UserRatingVector user) {
        ItemItemModel model = predictor.getModel();
    	if (model.getBaselinePredictor() != null) {
            return model.getItemUniverse();
        } else {
            LongSet items = new LongOpenHashSet();
            LongIterator iter = user.keySet().iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                for (IndexedItemScore n: model.getNeighbors(item)) {
                    items.add(model.getItem(n.getIndex()));
                }
            }
            return items;
        }
    }
}