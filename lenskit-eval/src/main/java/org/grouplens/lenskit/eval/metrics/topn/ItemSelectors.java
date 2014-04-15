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
package org.grouplens.lenskit.eval.metrics.topn;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Random;

/**
 * Class providing various item selectors.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class ItemSelectors {
    private static Logger logger = LoggerFactory.getLogger(ItemSelectors.class);
    private ItemSelectors() {}

    /**
     * Item selector that selects all items.
     * @return An item selector that selects the universe of items.
     */
    public static ItemSelector allItems() {
        return SingletonSelectors.ALL_ITEMS;
    }

    /**
     * Item selector that selects test items.
     * @return An item selector that selects the items from the user's test set.
     */
    public static ItemSelector testItems() {
        return SingletonSelectors.TEST_ITEMS;
    }

    /**
     * Item selector that selects training items.
     * @return An item selector that selects the items from the user's training set.
     */
    public static ItemSelector trainingItems() {
        return SingletonSelectors.TRAIN_ITEMS;
    }

    /**
     * Select items where the user's test rating matches a condition.  For example, using
     * the matcher {@link org.hamcrest.Matchers#greaterThanOrEqualTo(Comparable)} will select
     * items that the user has rated at least a particular value.
     *
     * @param matcher The matcher for test ratings.
     * @return An item selector that selects items with matching test ratings.
     */
    public static ItemSelector testRatingMatches(final Matcher<Double> matcher) {
        return new TestRatingMatcherItemSelector(matcher);
    }

    public static ItemSelector allItemsExcept(final ItemSelector base) {
        return new ComplementItemSelector(base);
    }

    /**
     * Select items selected by a selector, plus additional randomly-selected items.
     *
     * @param base The base selector (e.g. {@link #testItems()}).
     * @param nRandom The number of random items to add.
     * @return An item selector that selects the items selected by {@code base} plus an additional
     * {@code nRandom} items.
     */
    public static ItemSelector addNRandom(final ItemSelector base, final int nRandom) {
        Preconditions.checkArgument(nRandom >= 0, "nRandom cannot be negative");
        Preconditions.checkArgument(nRandom >= 0, "nRandom cannot be negative");
        return union(base, randomSubset(allItemsExcept(base), nRandom));
    }

    /**
     * Randomly select items selected by another selector.
     *
     * @param base The base selector (e.g. {@link #allItems()}).
     * @param n The number of random items to select.
     * @return An item selector that selects {@code n} items from the items selected by {@code base}, 
     * or simply the items selected by {@code base} if there are fewer then {@code n} items selected.
     */
    public static ItemSelector randomSubset(final ItemSelector base, final int n) {
        return new RandomSubsetItemSelector(base, n);
    }

    /**
     * Randomly selects items from the universe.
     * 
     * Short for {@code ItemSelectors.randomSubset(ItemSelectors.allItems, n)}
     */
    public static ItemSelector nRandom(final int n) {
        return randomSubset(allItems(), n);
    }

    /**
     * Select the most popular items.
     * @param n The number of items to select.
     * @return A selector factory that will select the {@code n} items with the most events.
     */
    public static ItemSelector mostPopular(int n) {
        return new PopularItemSelector(n);
    }


    /**
     * Selects the set difference between two other selectors
     * 
     * @param selectorToKeep selects items that are to be kept
     * @param selectorToNotKeep selects items that are to be excluded
     * @return an item selector which selects every item that is selected by {@code selectorToKeep} 
     * and not selected by {@code selectorToNotKeep}. 
     */
    public static ItemSelector setDifference(final ItemSelector selectorToKeep, final ItemSelector selectorToNotKeep) {
        return new SetDifferenceItemSelector(selectorToKeep, selectorToNotKeep);
    }
    
    /**
     * selects any items selected by at least one of two other selectors.
     */
    public static ItemSelector union(final ItemSelector selectorOne, final ItemSelector selectorTwo) {
        return new SetUnionItemSelector(selectorOne, selectorTwo);
    }

    /**
     * Select a fixed collection of items.
     * @param items The set of items. to select.
     * @return A selector that selects {@code items}.
     */
    public static ItemSelector fixed(final Collection<Long> items) {
        return new FixedItemSelector(items);
    }

    /**
     * Select a fixed collection of items.
     * @param items The items to select.
     * @return A selector that selects {@code items}.
     */
    public static ItemSelector fixed(long... items) {
        return fixed(LongArrayList.wrap(items));
    }

    /**
     * Get the universe from a recommender.
     * @param rec The recommender.
     * @return The recommender's universe.
     */
    static LongSet getUniverse(Recommender rec) {
        Preconditions.checkNotNull(rec, "recommender");
        return UNIVERSE_CACHE.getUnchecked(rec);
    }

    /**
     * A cache of item universes for recommenders.
     */
    private static LoadingCache<Recommender,LongSet> UNIVERSE_CACHE =
            CacheBuilder.newBuilder()
                        .weakKeys()
                        .build(new UniverseLoader());

    /**
     * Cache loader to extract the item universe from a recommender.
     */
    private static class UniverseLoader extends CacheLoader<Recommender,LongSet> {
        public LongSet load(Recommender rec) throws Exception {
            LenskitRecommender lkrec = (LenskitRecommender) rec;
            ItemDAO idao = lkrec.get(ItemDAO.class);
            if (idao == null) {
                logger.warn("Recommender has no item DAO");
                return LongSets.EMPTY_SET;
            } else {
                return idao.getItemIds();
            }
        }
    }


    private static enum SingletonSelectors implements ItemSelector {
        ALL_ITEMS {
            @Override
            public LongSet select(TestUser user) {
                return getUniverse(user.getRecommender());
            }
        },
        TEST_ITEMS {
            @Override
            public LongSet select(TestUser user) {
                return user.getTestHistory().itemSet();
            }
        },
        TRAIN_ITEMS {
            @Override
            public LongSet select(TestUser user) {
                return user.getTrainHistory().itemSet();
            }
        }
    }

    private static class TestRatingMatcherItemSelector implements ItemSelector {
        private final Matcher<Double> matcher;

        public TestRatingMatcherItemSelector(Matcher<Double> matcher) {
            this.matcher = matcher;
        }

        @Override
        public LongSet select(TestUser user) {
            LongSet items = new LongOpenHashSet();
            SparseVector vec = user.getTestRatings();
            for (VectorEntry e: vec.fast()) {
                if (matcher.matches(e.getValue())) {
                    items.add(e.getKey());
                }
            }
            return items;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestRatingMatcherItemSelector that = (TestRatingMatcherItemSelector) o;

            if (!matcher.equals(that.matcher)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return matcher.hashCode();
        }
    }

    private static class ComplementItemSelector implements ItemSelector {
        private final ItemSelector base;

        public ComplementItemSelector(ItemSelector base) {
            this.base = base;
        }

        @Override
        public LongSet select(TestUser user) {
            LongSet universe = UNIVERSE_CACHE.getUnchecked(user.getRecommender());
            return LongUtils.setDifference(universe, base.select(user));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComplementItemSelector that = (ComplementItemSelector) o;

            if (!base.equals(that.base)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return base.hashCode();
        }
    }

    private static class SetDifferenceItemSelector implements ItemSelector {
        private final ItemSelector selectorToKeep;
        private final ItemSelector selectorToNotKeep;

        public SetDifferenceItemSelector(ItemSelector selectorToKeep, ItemSelector selectorToNotKeep) {
            this.selectorToKeep = selectorToKeep;
            this.selectorToNotKeep = selectorToNotKeep;
        }

        @Override
        public LongSet select(TestUser user) {
            LongSet l1 = selectorToKeep.select(user);
            LongSet l2 = selectorToNotKeep.select(user);
            return LongUtils.setDifference(l1, l2);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SetDifferenceItemSelector that = (SetDifferenceItemSelector) o;

            if (!selectorToKeep.equals(that.selectorToKeep)) return false;
            if (!selectorToNotKeep.equals(that.selectorToNotKeep)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = selectorToKeep.hashCode();
            result = 31 * result + selectorToNotKeep.hashCode();
            return result;
        }
    }

    private static class SetUnionItemSelector implements ItemSelector {
        private final ItemSelector selectorOne;
        private final ItemSelector selectorTwo;

        public SetUnionItemSelector(ItemSelector selectorOne, ItemSelector selectorTwo) {
            this.selectorOne = selectorOne;
            this.selectorTwo = selectorTwo;
        }

        @Override
        public LongSet select(TestUser user) {
            LongSet items = new LongOpenHashSet(selectorOne.select(user));
            items.addAll(selectorTwo.select(user));
            return items;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SetUnionItemSelector that = (SetUnionItemSelector) o;

            if (!selectorOne.equals(that.selectorOne)) return false;
            if (!selectorTwo.equals(that.selectorTwo)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = selectorOne.hashCode();
            result = 31 * result + selectorTwo.hashCode();
            return result;
        }
    }

    private static class FixedItemSelector implements ItemSelector {
        private final LongSet selected;

        public FixedItemSelector(Collection<Long> items) {
            selected = LongUtils.packedSet(items);
        }

        @Override
        public LongSet select(TestUser user) {
            return selected;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FixedItemSelector that = (FixedItemSelector) o;

            if (!selected.equals(that.selected)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return selected.hashCode();
        }
    }

    private static class RandomSubsetItemSelector implements ItemSelector {
        private final ItemSelector delegate;
        private final int count;

        public RandomSubsetItemSelector(ItemSelector base, int n) {
            delegate = base;
            count = n;
        }

        @Override
        public LongSet select(TestUser user) {
            LenskitRecommender lkr = (LenskitRecommender) user.getRecommender();
            Random rng = null;
            if (lkr != null) {
                rng = lkr.get(Random.class);
            }
            if (rng == null) {
                rng = new Random();
            }
            LongSet items = delegate.select(user);
            if (items.size() <= count) {
                return items;
            }
            return LongUtils.randomSubset(items, count, rng);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RandomSubsetItemSelector that = (RandomSubsetItemSelector) o;

            if (count != that.count) return false;
            if (!delegate.equals(that.delegate)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = delegate.hashCode();
            result = 31 * result + count;
            return result;
        }
    }

    private static class PopularItemSelector implements ItemSelector, Function<Recommender,LongSet> {
        private final int count;
        private final LoadingCache<Recommender,LongSet> cache;

        public PopularItemSelector(int n) {
            count = n;
            cache = CacheBuilder.newBuilder()
                                .weakKeys()
                                .build(CacheLoader.from(this));
        }

        @Override
        public LongSet select(TestUser user) {
            return cache.getUnchecked(user.getRecommender());
        }

        @Nullable
        @Override
        public LongSet apply(@Nullable Recommender input) {
            if (input == null) {
                return LongSets.EMPTY_SET;
            }
            LenskitRecommender rec = (LenskitRecommender) input;
            ItemEventDAO idao = rec.get(ItemEventDAO.class);
            ScoredItemAccumulator accum = new TopNScoredItemAccumulator(count);
            Cursor<ItemEventCollection<Event>> items = idao.streamEventsByItem();
            try {
                for (ItemEventCollection<Event> item: items) {
                    accum.put(item.getItemId(), item.size());
                }
            } finally {
                items.close();
            }
            return accum.finishSet();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PopularItemSelector that = (PopularItemSelector) o;

            if (count != that.count) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return count;
        }
    }
}
