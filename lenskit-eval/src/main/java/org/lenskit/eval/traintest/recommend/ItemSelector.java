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
package org.lenskit.eval.traintest.recommend;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.lenskit.api.Recommender;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.LongSortedArraySet;

import java.util.Random;
import java.util.Set;

/**
 * Select items for use in recommendation or
 */
public abstract class ItemSelector {
    /**
     * Select a set of items for recommendation.
     * @param universe The universe of all items.
     * @param recommender The recommender, in case additional information is needed form it.
     * @param user The user being tested.
     * @return A set of items.
     */
    public abstract LongSet selectItems(LongSet universe, Recommender recommender, TestUser user);

    /**
     * Get an item selector that returns null.  When used as the set of candidate items, this will select the default
     * candidate set.
     *
     * @return An item selector that always returns {@code null}.
     */
    public static ItemSelector nullSelector() {
        return new NullItemSelector();
    }

    /**
     * Get an item selector that returns a fixed set of items.
     * @param items The items to return.
     */
    public static ItemSelector fixed(final LongSet items) {
        return new FixedItemSelector(items);
    }

    public static ItemSelector fixed(long... items) {
        return fixed(LongUtils.packedSet(items));
    }

    /**
     * Compile an item selector from a Groovy expression.  Two useful objects are available to this expression:
     *
     * allItems
     * :   A set of all item IDs in the system.
     *
     * user
     * :   The current user, as a {@link TestUser}.
     *
     * The expression is compiled as a script with a base class of {@link ItemSelectScript};
     * see that for more details.
     *
     * @param expr A Groovy expression.  Can be `null`, in which case {@link #nullSelector()} is returned.
     */
    public static ItemSelector compileSelector(String expr) {
        if (expr == null) {
            return nullSelector();
        }
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(ItemSelectScript.class.getName());
        GroovyShell shell = new GroovyShell(config);
        Script script = shell.parse(expr);
        return new GroovyItemSelector((ItemSelectScript) script, expr);
    }

    public static ItemSelector allItems() {
        return compileSelector("allItems");
    }

    public static ItemSelector userTestItems() {
        // FIXME Cache this selector
        return compileSelector("user.testItems");
    }

    public static ItemSelector userTrainItems() {
        // FIXME Cache this selector
        return compileSelector("user.trainItems");
    }

    /**
     * Base class defining the environment in which item selectors are evaluated.
     */
    public abstract static class ItemSelectScript extends Script {
        private final Random random = new Random();
        private LongSortedArraySet allItems;
        private TestUser testUser;
        private Recommender recommender;

        void setup(LongSet universe, Recommender rec, TestUser user) {
            allItems = LongUtils.packedSet(universe);
            testUser = user;
            recommender = rec;
        }

        /**
         * Get the recommender object.
         * @return The recommender object.
         */
        public Recommender getRecommender() {
            return recommender;
        }

        /**
         * Get the set of all items.
         * @return The set of all items.
         */
        public LongSortedArraySet getAllItems() {
            return allItems;
        }

        /**
         * Get the user being tested.
         * @return The user being tested, with their training and test data.
         */
        public TestUser getUser() {
            return testUser;
        }

        /**
         * Pick a random subset of a set of items.
         * @param items The set of items to pick from.
         * @param n the number of items to select.
         * @return A random subset of `items` of size at most `n`.
         */
        public LongSet pickRandom(Set<Long> items, int n) {
            return LongUtils.randomSubset(LongUtils.asLongSet(items), n, random);
        }

        /**
         * Method that returns all items except the ones present in the user's test
         * or train sets.
         */
        public LongSet getUnseenItems(TestUser user){
            return LongUtils.setDifference(allItems, user.getSeenItems());
        }

        public LongSet randomUnseen(TestUser user, int n) {
            return allItems.randomSubset(random, n, user.getSeenItems());
        }
    }

    /**
     * Item selector based on a Groovy script.
     */
    public static class GroovyItemSelector extends ItemSelector {
        private final ItemSelectScript script;
        private final String source;

        GroovyItemSelector(ItemSelectScript scr, String src) {
            script = scr;
            source = src;
        }

        /**
         * Get the Groovy source of this item selector.
         * @return The item selector's source.
         */
        public String getSource() {
            return source;
        }

        @SuppressWarnings("unchecked")
        @Override
        public synchronized LongSet selectItems(LongSet universe, Recommender recommender, TestUser user) {
            script.setup(universe, recommender, user);
            Set<Long> set = (Set<Long>) script.run();
            return LongUtils.asLongSet(set);
        }

        @Override
        public String toString() {
            return "GroovyItemSelector{" + source + "}";
        }
    }

    private static class NullItemSelector extends ItemSelector {
        @Override
        public LongSet selectItems(LongSet universe, Recommender recommender, TestUser user) {
            return null;
        }

        @Override
        public String toString() {
            return "ItemSelector{null}";
        }
    }

    private static class FixedItemSelector extends ItemSelector {
        private final LongSet items;

        public FixedItemSelector(LongSet items) {
            this.items = items;
        }

        @Override
        public LongSet selectItems(LongSet universe, Recommender recommender, TestUser user) {
            return items;
        }

        @Override
        public String toString() {
            return "ItemSelector{" + items + "}";
        }
    }
}
