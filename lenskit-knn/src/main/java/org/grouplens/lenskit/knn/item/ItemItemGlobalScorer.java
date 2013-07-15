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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.basic.AbstractGlobalItemScorer;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Score items based on the basket of items using an item-item CF model.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemGlobalScorer extends AbstractGlobalItemScorer {
    protected final ItemItemModel model;
    @Nonnull
    protected final
    NeighborhoodScorer scorer;
    @Nonnull
    protected final
    ItemScoreAlgorithm algorithm;

    @Inject
    public ItemItemGlobalScorer(ItemItemModel m, ItemScoreAlgorithm algo) {
        model = m;
        // The global item scorer use the SimilaritySumNeighborhoodScorer for the unary ratings
        this.scorer = new SimilaritySumNeighborhoodScorer();
        algorithm = algo;
    }

    @Override
    public void globalScore(@Nonnull Collection<Long> queryItems,
                            @Nonnull MutableSparseVector output) {
        // create the unary rating for the items
        LongSet qItems = new LongSortedArraySet(queryItems);
        MutableSparseVector basket = new MutableSparseVector(qItems, 1.0);

        output.clear();
        algorithm.scoreItems(model, basket, output, scorer);
    }
}
