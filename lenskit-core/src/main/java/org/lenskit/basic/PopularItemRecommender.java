/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemBasedItemRecommender;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.InteractionStatistics;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Recommend the most popular items. More efficient than using a popularity rank scorer.
 */
public class PopularItemRecommender extends AbstractItemRecommender implements ItemRecommender, ItemBasedItemRecommender {
    private final DataAccessObject data;
    private final InteractionStatistics statistics;

    /**
     * Create a new popular item recommender.
     * @param stats The interaction statistics.
     */
    @Inject
    public PopularItemRecommender(InteractionStatistics stats, DataAccessObject dao) {
        data = dao;
        statistics = stats;
    }

    private LongList recommendWithPredicate(int n, LongPredicate filter) {
        LongList items = statistics.getItemsByPopularity();
        LongList list = new LongArrayList(items.size());
        LongStream str = IntStream.range(0, items.size()).mapToLong(items::getLong);
        if (filter != null) {
            str = str.filter(filter);
        }
        if (n > 0) {
            str = str.limit(n);
        }
        str.forEachOrdered(list::add);
        return list;
    }

    private LongList recommendWithSets(int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        LongSet cs = LongUtils.asLongSet(candidates);
        LongSet es = LongUtils.asLongSet(exclude);
        LongPredicate f;
        if (cs != null) {
            if (es != null) {
                f = i -> cs.contains(i) && !es.contains(i);
            } else {
                f = cs::contains;
            }
        } else if (es != null) {
            f = i -> !es.contains(i);
        } else {
            f = null;
        }

        return recommendWithPredicate(n, f);
    }


    @Override
    public List<Long> recommendRelatedItems(long reference) {
        return recommendRelatedItems(reference, -1);
    }

    @Override
    public List<Long> recommendRelatedItems(long reference, int n) {
        return recommendWithPredicate(n, i -> i != reference);
    }

    @Override
    public List<Long> recommendRelatedItems(Set<Long> basket) {
        return recommendRelatedItems(basket, -1);
    }

    @Override
    public List<Long> recommendRelatedItems(Set<Long> basket, int n) {
        LongSet lbk = LongUtils.asLongSet(basket);
        return recommendWithPredicate(n, i -> !lbk.contains(i));
    }

    @Override
    public LongList recommendRelatedItems(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return recommendWithSets(n, candidates, exclude);
    }


    @Override
    public ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return recommendRelatedItems(basket, n, candidates, exclude)
                .stream()
                .map(i -> Results.create(i, statistics.getInteractionCount(i)))
                .collect(Results.listCollector());
    }

    @Override
    protected LongList recommend(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        if (exclude == null) {
            exclude = data.query(statistics.getEntityType())
                          .withAttribute(CommonAttributes.USER_ID, user)
                          .valueSet(CommonAttributes.ITEM_ID);
        }
        return recommendWithSets(n, candidates, exclude);
    }

    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidates, @Nullable LongSet exclude) {
        return recommend(user, n, candidates, exclude)
                .stream()
                .map(i -> Results.create(i, statistics.getInteractionCount(i)))
                .collect(Results.listCollector());
    }
}
