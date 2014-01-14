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
package org.grouplens.lenskit.data.dao.packed;

import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultBoolean;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.grapht.annotation.DefaultString;
import org.grouplens.lenskit.core.Parameter;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.data.history.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.io.*;
import java.lang.annotation.*;
import java.util.EnumSet;
import java.util.List;

/**
 * A DAO that has a snapshot of the rating data.  This snapshot is stored in a {@link BinaryRatingDAO}.
 * The file is defined with two parameters: the directory {@code lenskit.model.dir}, and the file
 * defined by {@link RatingSnapshotPath}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(RatingSnapshotDAO.Builder.class)
public class RatingSnapshotDAO implements EventDAO, UserEventDAO, ItemEventDAO, UserDAO, ItemDAO, Serializable {
    private static final long serialVersionUID = -1L;
    private static final Logger logger = LoggerFactory.getLogger(RatingSnapshotDAO.class);

    private final String daoPath;
    private final BinaryRatingDAO delegate;

    private RatingSnapshotDAO(String path, BinaryRatingDAO dao) {
        daoPath = path;
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

    private static File getSnapshotFile(String path) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            String root = System.getProperty("lenskit.model.dir");
            if (root != null) {
                file = new File(root, path);
            }
        }
        return file;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SerialProxy(daoPath);
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String path;

        public SerialProxy(String p) {
            path = p;
        }

        private Object readResolve() throws ObjectStreamException {
            File file = getSnapshotFile(path);
            logger.debug("using binary rating DAO in {}", file);
            try {
                return new RatingSnapshotDAO(path, BinaryRatingDAO.open(file));
            } catch (IOException e) {
                ObjectStreamException ex = new InvalidObjectException("cannot load rating file");
                ex.initCause(e);
                throw ex;
            }
        }
    }

    @Documented
    @Qualifier
    @Parameter(String.class)
    @DefaultString("ratings.pack")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    public static @interface RatingSnapshotPath {}

    @Documented
    @Qualifier
    @Parameter(Boolean.class)
    @DefaultBoolean(true)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    public static @interface UseTimestamps {}

    public static class Builder implements Provider<RatingSnapshotDAO> {
        private final String path;
        private final EventDAO dao;
        private final boolean useTimestamps;

        @Inject
        public Builder(@RatingSnapshotPath String path,
                       @Transient EventDAO dao,
                       @UseTimestamps boolean ts) {
            this.path = path;
            this.dao = dao;
            useTimestamps = ts;
        }

        @Override
        public RatingSnapshotDAO get() {
            File file = getSnapshotFile(path);
            File tmpFile = new File(file.getParentFile(), file.getName() + ".tmp");
            logger.debug("packing ratings to {}", tmpFile);

            EnumSet<BinaryFormatFlag> flags = EnumSet.noneOf(BinaryFormatFlag.class);
            SortOrder order = SortOrder.ANY;
            if (useTimestamps) {
                flags.add(BinaryFormatFlag.TIMESTAMPS);
                order = SortOrder.TIMESTAMP;
            }

            Closer closer = Closer.create();
            try {
                try {
                    BinaryRatingPacker packer = closer.register(BinaryRatingPacker.open(tmpFile, flags));
                    Cursor<Rating> ratings = closer.register(dao.streamEvents(Rating.class, order));
                    packer.writeRatings(ratings);
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
                logger.debug("renaming {} -> {}", tmpFile, file);
                if (!tmpFile.renameTo(file)) {
                    logger.error("cannot rename {} to {}", tmpFile, file);
                    throw new RuntimeException("error renaming packed file");
                }
                return new RatingSnapshotDAO(path, BinaryRatingDAO.open(file));
            } catch (IOException ex) {
                throw new RuntimeException("error packing ratings", ex);
            }
        }
    }
}
