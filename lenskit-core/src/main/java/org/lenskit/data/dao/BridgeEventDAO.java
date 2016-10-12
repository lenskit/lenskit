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

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.inject.Inject;

/**
 * Bridge implementation of the event DAO to migrate from new DAO architecture.
 */
public class BridgeEventDAO implements EventDAO {
    private final DataAccessObject delegate;

    @Inject
    public BridgeEventDAO(DataAccessObject dao) {
        delegate = dao;
    }

    @Override
    public ObjectStream<Event> streamEvents() {
        // since we don't really work with non-rating entities anywhere, just use this
        return (ObjectStream) streamEvents(Rating.class);
    }

    @Override
    public <E extends Event> ObjectStream<E> streamEvents(Class<E> type) {
        if (Entity.class.isAssignableFrom(type)) {
            return delegate.query((Class) type).stream();
        } else {
            return ObjectStreams.empty();
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <E extends Event> ObjectStream<E> streamEvents(Class<E> type, SortOrder order) {
        if (Entity.class.isAssignableFrom(type)) {
            Query query = delegate.query((Class) type);
            switch (order) {
            case USER:
                query = query.orderBy(CommonAttributes.USER_ID);
                break;
            case ITEM:
                query = query.orderBy(CommonAttributes.ITEM_ID);
                break;
            case TIMESTAMP:
                query = query.orderBy(CommonAttributes.TIMESTAMP);
                break;
            default:
                throw new IllegalArgumentException("invalid sort order " + order);
            }
            return query.stream();
        } else {
            return ObjectStreams.empty();
        }
    }
}
