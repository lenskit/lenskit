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
package org.grouplens.lenskit.data.history;

import com.google.common.base.Function;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.data.event.Event;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract implementation of {@link UserHistory} to provide default
 * implementations of convenience methods.
 *
 * @param <E> The type of event this history contains.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public (may require additional method implementations across minor revisions)
 */
public abstract class AbstractUserHistory<E extends Event> extends AbstractList<E> implements UserHistory<E> {
    @SuppressWarnings("rawtypes")
    private transient volatile Map<Function, Object> memTable;

    @Override
    public LongSet itemSet() {
        return memoize(ItemSetFunction.INSTANCE);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T memoize(Function<? super UserHistory<E>, ? extends T> func) {
        Map<Function,Object> table = memTable;
        if (table == null) {
            synchronized (this) {
                table = memTable;
                if (table == null) {
                    memTable = table = new ConcurrentHashMap<Function, Object>();
                }
            }
        }

        if (!table.containsKey(func)) {
            // worst case scenario: we compute the function twice. This is permissible.
            table.put(func, func.apply(this));
        }
        return (T) table.get(func);
    }

    private static enum ItemSetFunction implements Function<UserHistory<? extends Event>, LongSet> {
        INSTANCE;

        @Nullable @Override
        public LongSet apply(@Nullable UserHistory<? extends Event> input) {
            if (input == null) {
                return null;
            } else {
                LongSet items = new LongOpenHashSet();
                for (Event e : input) {
                    items.add(e.getItemId());
                }
                return items;
            }
        }
    }
}
