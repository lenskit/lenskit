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
package org.lenskit.data.dao;

import com.google.common.base.Function;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.events.Event;
import org.lenskit.data.history.History;
import org.lenskit.data.history.UserHistory;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Bridge to transition user event DAO use to new DAOs.
 */
@SuppressWarnings("deprecation")
public class BridgeUserEventDAO implements UserEventDAO {
    private final DataAccessObject delegate;

    /**
     * Construct a new bridge user DAO.
     * @param dao The underlying DAO.
     */
    @Inject
    public BridgeUserEventDAO(DataAccessObject dao) {
        delegate = dao;
    }

    @Override
    public ObjectStream<UserHistory<Event>> streamEventsByUser() {
        // since we don't really work with non-rating entities anywhere, just use this
        return (ObjectStream) streamEventsByUser(Rating.class);
    }

    @Override
    public <E extends Event> ObjectStream<UserHistory<E>> streamEventsByUser(Class<E> type) {
        ObjectStream<IdBox<List<? extends Entity>>> stream =
                (ObjectStream) delegate.query((Class<? extends Entity>) type)
                                       .groupBy(CommonAttributes.USER_ID)
                                       .stream();
        return ObjectStreams.transform(stream, new Function<IdBox<List<? extends Entity>>, UserHistory<E>>() {
            @Nullable
            @Override
            public UserHistory<E> apply(@Nullable IdBox<List<? extends Entity>> input) {
                assert input != null;
                return History.forUser(input.getId(), (List) input.getValue());
            }
        });
    }

    @Override
    public UserHistory<Event> getEventsForUser(long user) {
        return (UserHistory) getEventsForUser(user, Rating.class);
    }

    @Nullable
    @Override
    public <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type) {
        return History.forUser(user, (List) delegate.query((Class<? extends Entity>) type)
                                                    .withAttribute(CommonAttributes.USER_ID, user)
                                                    .get());
    }

    public static class DynamicProvider implements Provider<UserEventDAO> {
        private final DataAccessObject dao;
        private final EventDAO eventDao;

        @Inject
        public DynamicProvider(@Nullable DataAccessObject dao, @Nullable EventDAO events) {
            this.dao = dao;
            this.eventDao = events;
        }

        @Override
        public UserEventDAO get() {
            if (dao != null) {
                return new BridgeUserEventDAO(dao);
            } else {
                return new PrefetchingUserEventDAO(eventDao);
            }
        }
    }
}
