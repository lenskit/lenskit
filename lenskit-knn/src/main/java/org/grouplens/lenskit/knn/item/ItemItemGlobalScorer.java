/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.AbstractGlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Score items based on the basket of items using an item-item CF model.
 * 
 * @author Shuo Chang <schang@cs.umn.edu>
 * 
 */
public class ItemItemGlobalScorer extends AbstractGlobalItemScorer implements
        ItemItemModelBackedGlobalScorer {
	protected final ItemItemModel model;
	protected final @Nonnull NeighborhoodScorer scorer;
    protected final @Nonnull ItemScoreAlgorithm algorithm;

    @Inject
	public ItemItemGlobalScorer(DataAccessObject dao, ItemItemModel m,
                                ItemScoreAlgorithm algo) {
		super(dao);
		model = m;
		// The global item scorer use the SimilaritySumNeighborhoodScorer for the unary ratings
		this.scorer = new SimilaritySumNeighborhoodScorer();
		algorithm = algo;
	}

    @Override
    public ItemItemModel getModel() {
        return model;
    }
    
    @Override
    public void globalScore(Collection<Long> queryItems,
                            MutableSparseVector output) {
		// create the unary rating for the items
        LongSet qItems = new LongSortedArraySet(queryItems);
        MutableSparseVector basket = new MutableSparseVector(qItems, 1.0);

        output.clear();
        algorithm.scoreItems(model, basket, output, scorer);
	}
	

	@Override
	public LongSet getScoreableItems(Collection<Long> queryItems) {
        // FIXME This method incorrectly assumes the model is symmetric
        LongSet items = new LongOpenHashSet();
        LongIterator iter = LongIterators.asLongIterator(queryItems.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            items.addAll(model.getNeighbors(item));
        }
        return items;
    }
}
