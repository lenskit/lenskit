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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.hamcrest.Matcher;

import java.util.Random;

/**
 * Class providing various item selectors.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class ItemSelectors {
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
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                // FIXME See about making this more efficient
                MutableSparseVector vec = RatingVectorUserHistorySummarizer.makeRatingVector(testData).mutableCopy();
                for (VectorEntry e: vec.fast()) {
                    if (!matcher.matches(e.getValue())) {
                        vec.unset(e);
                    }
                }
                return vec.immutable().keySet();
            }
        };
    }

    public static ItemSelector allItemsExcept(final ItemSelector base) {
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                return LongUtils.setDifference(universe, base.select(trainingData, testData, universe));
            }
        };
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
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                // FIXME The RNG should come from configuration
                Random rng = new Random();
                LongSortedSet initial = LongUtils.packedSet(base.select(trainingData, testData, universe));
                LongSortedSet selected = LongUtils.randomSubset(universe, nRandom, initial, rng);
                return LongUtils.setUnion(initial, selected);
            }
        };
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
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                // FIXME The RNG should come from configuration
                Random rng = new Random();
                LongSet newUniverse = base.select(trainingData, testData, universe);
                if (newUniverse.size() <= n) {
                    return newUniverse;
                }
                return LongUtils.randomSubset(newUniverse, n, rng);
            }
        };
    }

    /**
     * Randomly selects items from the universe.
     * 
     * Short for {@code ItemSelectors.randomSubset(ItemSelectors.allItems, n)}
     */
    public static ItemSelector nRandom(final int n) {
        return ItemSelectors.randomSubset(ItemSelectors.allItems(), n);
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
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                LongSet l1 = selectorToKeep.select(trainingData, testData, universe);
                LongSet l2 = selectorToNotKeep.select(trainingData, testData, universe);
                return LongUtils.setDifference(l1, l2);
            }
        };
    }
    
    /**
     * selects any items selected by at least one of two other selectors.
     */
    public static ItemSelector union(final ItemSelector selectorOne, final ItemSelector selectorTwo) {
        return new ItemSelector() {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                LongSet l1 = selectorOne.select(trainingData, testData, universe);
                LongSet l2 = selectorTwo.select(trainingData, testData, universe);
                // @Review Is this reasonably efficient?
                return LongUtils.setUnion(LongUtils.packedSet(l1), LongUtils.packedSet(l2));
            }
        };
    }

    private static enum SingletonSelectors implements ItemSelector {
        ALL_ITEMS {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                return universe;
            }
        },
        TEST_ITEMS {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                LongSet items = new LongOpenHashSet(testData.size());
                for (Event e: CollectionUtils.fast(testData)) {
                    items.add(e.getItemId());
                }
                return items;
            }
        },
        TRAIN_ITEMS {
            @Override
            public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
                LongSet items = new LongOpenHashSet(testData.size());
                for (Event e: CollectionUtils.fast(trainingData)) {
                    items.add(e.getItemId());
                }
                return items;
            }
        }
    }
}
