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
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.AbstractRecommenderService;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RatingRecommender;
import org.grouplens.lenskit.RecommenderBuilder;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Generate predictions and recommendations using item-item CF.
 *
 * This class implements an item-item collaborative filter backed by a particular
 * {@link ItemItemModel}.  Client code will usually use a
 * {@link RecommenderBuilder} to get one of these.
 *
 * To modify the recommendation or prediction logic, do the following:
 *
 * <ul>
 * <li>Extend {@link ItemItemRecommenderBuilder}, reimplementing the
 * {@link ItemItemRecommenderBuilder#createRecommender(ItemItemModel)} method
 * to create an instance of your new class rather than this one.
 * <li>Configure Guice to inject your new recommender builder.
 * </ul>
 *
 * @todo Document how this class works.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public class ItemItemRecommenderService extends AbstractRecommenderService implements Serializable {
    private static final long serialVersionUID = 3157980766584927863L;
    protected final @Nonnull ItemItemModel model;

    /**
     * Construct a new recommender from an item-item recommender predictor.
     * @param predictor The backing predictor for the new recommender.
     */
    public ItemItemRecommenderService(@Nonnull ItemItemModel model) {
        this.model = model;
    }

    @Override
    public ItemItemRatingPredictor getRatingPredictor() {
        // FIXME Don't allocate all the time - use Guice
        return new ItemItemRatingPredictor(model);
    }

    @Override
    public RatingRecommender getRatingRecommender() {
        // FIXME Don't allocate all the time - use Guice
        return new ItemItemRatingRecommender(getRatingPredictor());
    }
}
