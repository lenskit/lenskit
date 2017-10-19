/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
