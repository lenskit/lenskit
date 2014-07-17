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
package org.grouplens.lenskit.data.dao;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * User DAO that streams the events to get user information.  The user set is fetched and memorized
 * once for each instance of this class.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PrefetchingUserDAO implements UserDAO, Describable {
    private final EventDAO eventDAO;
    private final Supplier<LongSet> userCache;

    /**
     * A function that wraps an event DAO in a prefetching user DAO.  If the DAO already
     * implements {@link UserDAO}, it is returned unwrapped.
     * @return A wrapper function to make user DAOs from event DAOs.
     */
    public static Function<EventDAO,UserDAO> wrapper() {
        return WrapperFunction.INSTANCE;
    }

    private static enum WrapperFunction implements Function<EventDAO,UserDAO> {
        INSTANCE;

        @Nullable
        @Override
        public UserDAO apply(@Nullable EventDAO input) {
            if (input instanceof UserDAO) {
                return (UserDAO) input;
            } else {
                return new PrefetchingUserDAO(input);
            }
        }
    }

    @Inject
    public PrefetchingUserDAO(EventDAO events) {
        eventDAO = events;
        userCache = Suppliers.memoize(new UserScanner());
    }

    @Override
    public LongSet getUserIds() {
        return userCache.get();
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("daoType", "User")
              .putField("delegate", eventDAO);
    }

    private class UserScanner implements Supplier<LongSet> {
        @Override
        public LongSet get() {
            LongSet us = new LongOpenHashSet();
            Cursor<Event> events = eventDAO.streamEvents();
            try {
                for (Event e: events.fast()) {
                    us.add(e.getUserId());
                }
            } finally {
                events.close();
            }
            return us;
        }
    }
}
