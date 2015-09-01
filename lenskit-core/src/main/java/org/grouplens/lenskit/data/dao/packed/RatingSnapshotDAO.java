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
package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.*;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.event.UseTimestamps;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.data.history.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * A DAO that has a snapshot of the rating data.  This snapshot is stored in a {@link BinaryRatingDAO}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(RatingSnapshotDAO.Builder.class)
public class RatingSnapshotDAO implements EventDAO, UserEventDAO, ItemEventDAO, UserDAO, ItemDAO, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RatingSnapshotDAO.class);

    private final BinaryRatingDAO delegate;

    private RatingSnapshotDAO(BinaryRatingDAO dao) {
        delegate = dao;
    }

    @Override
    public Cursor<Event> streamEvents() {
        return delegate.streamEvents();
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return delegate.streamEvents(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        return delegate.streamEvents(type, order);
    }

    @Override
    public LongSet getItemIds() {
        return delegate.getItemIds();
    }

    @Override
    public Cursor<ItemEventCollection<Event>> streamEventsByItem() {
        return delegate.streamEventsByItem();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<ItemEventCollection<E>> streamEventsByItem(Class<E> type) {
        return delegate.streamEventsByItem(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> getEventsForItem(long item) {
        return delegate.getEventsForItem(item);
    }

    @Nullable
    @Override
    public <E extends Event> List<E> getEventsForItem(long item, Class<E> type) {
        return delegate.getEventsForItem(item, type);
    }

    @Nullable
    @Override
    public LongSet getUsersForItem(long item) {
        return delegate.getUsersForItem(item);
    }

    @Override
    public LongSet getUserIds() {
        return delegate.getUserIds();
    }

    @Override
    public Cursor<UserHistory<Event>> streamEventsByUser() {
        return delegate.streamEventsByUser();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<UserHistory<E>> streamEventsByUser(Class<E> type) {
        return delegate.streamEventsByUser(type);
    }

    @Nullable
    @Override
    public UserHistory<Event> getEventsForUser(long user) {
        return delegate.getEventsForUser(user);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type) {
        return delegate.getEventsForUser(user, type);
    }

    public static class Builder implements Provider<RatingSnapshotDAO> {
        private final EventDAO dao;
        private final boolean useTimestamps;

        @Inject
        public Builder(@Transient EventDAO dao,
                       @UseTimestamps boolean ts) {
            this.dao = dao;
            useTimestamps = ts;
        }

        @Override
        public RatingSnapshotDAO get() {
            File file;
            try {
                file = File.createTempFile("ratings", ".pack");
            } catch (IOException e) {
                throw new RuntimeException("cannot create temporary file");
            }
            file.deleteOnExit();
            logger.debug("packing ratings to {}", file);

            EnumSet<BinaryFormatFlag> flags = EnumSet.noneOf(BinaryFormatFlag.class);
            SortOrder order = SortOrder.ANY;
            if (useTimestamps) {
                flags.add(BinaryFormatFlag.TIMESTAMPS);
                order = SortOrder.TIMESTAMP;
            }

            try {
                try (BinaryRatingPacker packer = BinaryRatingPacker.open(file, flags);
                     Cursor<Rating> ratings = dao.streamEvents(Rating.class, order)) {
                    packer.writeRatings(ratings);
                }
                BinaryRatingDAO result = BinaryRatingDAO.open(file);
                // try to delete the file early, helps keep things clean on Unix
                if (file.delete()) {
                    logger.debug("unlinked {}, will be deleted when freed", file);
                }
                return new RatingSnapshotDAO(result);
            } catch (IOException ex) {
                throw new RuntimeException("error packing ratings", ex);
            }
        }
    }
}
