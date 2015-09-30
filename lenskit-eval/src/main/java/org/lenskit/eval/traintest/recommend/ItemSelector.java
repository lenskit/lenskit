/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.traintest.recommend;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.grouplens.lenskit.collections.LongUtils;
import org.lenskit.eval.traintest.TestUser;

import java.util.Random;
import java.util.Set;

/**
 * Select items for use in recommendation or
 */
public abstract class ItemSelector {
    public abstract LongSet selectItems(LongSet universe, TestUser user);

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

    public static ItemSelector userTestItems() {
        // FIXME Cache this selector
        return compileSelector("user.testItems");
    }

    public abstract static class ItemSelectScript extends Script {
        private final Random random = new Random();
        private LongSet allItems;
        private TestUser testUser;

        void setup(LongSet universe, TestUser user) {
            allItems = universe;
            testUser = user;
        }

        public LongSet getAllItems() {
            return allItems;
        }

        public TestUser getUser() {
            return testUser;
        }

        public LongSet pickRandom(Set<Long> items, int n) {
            return LongUtils.randomSubset(LongUtils.asLongSet(items), n, random);
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
        public LongSet selectItems(LongSet universe, TestUser user) {
            script.setup(universe, user);
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
        public LongSet selectItems(LongSet universe, TestUser user) {
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
        public LongSet selectItems(LongSet universe, TestUser user) {
            return items;
        }

        @Override
        public String toString() {
            return "ItemSelector{" + items + "}";
        }
    }
}
