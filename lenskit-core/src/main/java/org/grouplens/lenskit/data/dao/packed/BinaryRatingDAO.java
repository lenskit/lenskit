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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.ItemEventCollection;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * DAO implementation using binary-packed data.  This DAO reads ratings from a compact binary format
 * using memory-mapped IO, so the data is efficiently readable (subject to available memory and
 * operating system caching logic) without expanding the Java heap.
 * <p>
 * To create a file compatible with this DAO, use the {@link BinaryRatingPacker} class or the
 * <tt>pack</tt> command in the LensKit command line tool.
 * <p>
 * Currently, serializing a binary rating DAO puts all the rating data into the serialized output
 * stream. When deserialized, the data be written back to a direct buffer (allocated with
 * {@link ByteBuffer#allocateDirect(int)}).  When deserializing this DAO, make sure your
 * system has enough virtual memory (beyond what is allowed for Java) to contain the entire data set.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
@DefaultProvider(BinaryRatingDAO.Loader.class)
public class BinaryRatingDAO implements EventDAO, UserEventDAO, ItemEventDAO, UserDAO, ItemDAO, Serializable, Describable {
    private static final long serialVersionUID = -1L;
    private static final Logger logger = LoggerFactory.getLogger(BinaryRatingDAO.class);

    @Nullable
    private final transient File backingFile;
    private final BinaryHeader header;
    private final ByteBuffer ratingData;
    private final BinaryIndexTable userTable;
    private final BinaryIndexTable itemTable;

    private BinaryRatingDAO(@Nullable File file, BinaryHeader hdr, ByteBuffer data, BinaryIndexTable users, BinaryIndexTable items) {
        Preconditions.checkArgument(data.position() == 0, "data is not at position 0");
        backingFile = file;
        header = hdr;
        ratingData = data;
        userTable = users;
        itemTable = items;
    }

    static BinaryRatingDAO fromBuffer(ByteBuffer buffer) {
        BinaryHeader header = BinaryHeader.fromHeader(buffer);
        assert buffer.position() >= BinaryHeader.HEADER_SIZE;
        ByteBuffer dup = buffer.duplicate();
        dup.limit(header.getRatingDataSize());

        ByteBuffer tableBuffer = buffer.duplicate();
        tableBuffer.position(tableBuffer.position() + header.getRatingDataSize());
        BinaryIndexTable utbl = BinaryIndexTable.fromBuffer(header.getUserCount(), tableBuffer);
        BinaryIndexTable itbl = BinaryIndexTable.fromBuffer(header.getItemCount(), tableBuffer);

        return new BinaryRatingDAO(null, header, dup.slice(), utbl, itbl);
    }

    /**
     * Open a binary rating DAO.
     * @param file The file to open.
     * @return A DAO backed by {@code file}.
     * @throws IOException If there is
     */
    public static BinaryRatingDAO open(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        try {
            FileChannel channel = input.getChannel();
            BinaryHeader header = BinaryHeader.read(channel);
            logger.info("Loading DAO with {} ratings of {} items from {} users",
                        header.getRatingCount(), header.getItemCount(), header.getUserCount());

            ByteBuffer data = channel.map(FileChannel.MapMode.READ_ONLY,
                                          channel.position(), header.getRatingDataSize());
            channel.position(channel.position() + header.getRatingDataSize());

            ByteBuffer tableBuffer = channel.map(FileChannel.MapMode.READ_ONLY,
                                                 channel.position(), channel.size() - channel.position());
            BinaryIndexTable utbl = BinaryIndexTable.fromBuffer(header.getUserCount(), tableBuffer);
            BinaryIndexTable itbl = BinaryIndexTable.fromBuffer(header.getItemCount(), tableBuffer);

            return new BinaryRatingDAO(file, header, data, utbl, itbl);
        } finally {
            input.close();
        }
    }

    private Object writeReplace() {
        return new SerialProxy(header, ratingData, userTable, itemTable);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("attempted to read BinaryRatingDAO without proxy");
    }

    private BinaryRatingList getRatingList() {
        return getRatingList(CollectionUtils.interval(0, header.getRatingCount()));
    }

    private BinaryRatingList getRatingList(IntList indexes) {
        return new BinaryRatingList(header.getFormat(), ratingData, indexes);
    }

    @Override
    public Cursor<Event> streamEvents() {
        return streamEvents(Event.class);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return streamEvents(type, SortOrder.ANY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        if (!type.isAssignableFrom(Rating.class)) {
            return Cursors.empty();
        }

        final Cursor<Rating> cursor;

        switch (order) {
        case ANY:
        case TIMESTAMP:
            cursor = getRatingList().cursor();
            break;
        case USER:
            cursor = Cursors.concat(Iterables.transform(userTable.entries(),
                                                        new EntryToCursorTransformer()));
            break;
        case ITEM:
            cursor = Cursors.concat(Iterables.transform(itemTable.entries(),
                                                        new EntryToCursorTransformer()));
            break;
        default:
            throw new IllegalArgumentException("unexpected sort order");
        }

        return (Cursor<E>) cursor;
    }

    @Override
    public LongSet getItemIds() {
        return itemTable.getKeys();
    }

    @Override
    public Cursor<ItemEventCollection<Event>> streamEventsByItem() {
        return streamEventsByItem(Event.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<ItemEventCollection<E>> streamEventsByItem(Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            // cast is safe, Rating extends E
            return (Cursor) Cursors.wrap(Collections2.transform(itemTable.entries(),
                                                                new ItemEntryTransformer()));
        } else {
            return Cursors.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> getEventsForItem(long item) {
        return getEventsForItem(item, Event.class);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <E extends Event> List<E> getEventsForItem(long item, Class<E> type) {
        IntList index = itemTable.getEntry(item);
        if (index == null) {
            return null;
        }

        if (!type.isAssignableFrom(Rating.class)) {
            // we only have ratings
            return ImmutableList.of();
        }

        return (List<E>) getRatingList(index);
    }

    @Nullable
    @Override
    public LongSet getUsersForItem(long item) {
        List<Rating> ratings = getEventsForItem(item, Rating.class);
        if (ratings == null) {
            return null;
        }

        LongSet users = new LongOpenHashSet(ratings.size());
        for (Rating rating: ratings) {
            users.add(rating.getUserId());
        }
        return users;
    }

    @Override
    public LongSet getUserIds() {
        return userTable.getKeys();
    }

    @Override
    public Cursor<UserHistory<Event>> streamEventsByUser() {
        return streamEventsByUser(Event.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<UserHistory<E>> streamEventsByUser(Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            // cast is safe, E super Rating
            return (Cursor) Cursors.wrap(Collections2.transform(userTable.entries(),
                                                                new UserEntryTransformer()));
        } else {
            return Cursors.empty();
        }
    }

    @Nullable
    @Override
    public UserHistory<Event> getEventsForUser(long user) {
        return getEventsForUser(user, Event.class);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type) {
        IntList index = userTable.getEntry(user);
        if (index == null) {
            return null;
        }

        if (!type.isAssignableFrom(Rating.class)) {
            return History.forUser(user);
        }

        return (UserHistory<E>) new BinaryUserHistory(user, getRatingList(index));
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        if (backingFile != null) {
            writer.putField("file", backingFile.getAbsolutePath())
                  .putField("mtime", backingFile.lastModified());
        } else {
            writer.putField("file", "/dev/null")
                  .putField("mtime", 0);
        }
        writer.putField("header", header.render());
    }

    private class EntryToCursorTransformer implements Function<Pair<Long, IntList>, Cursor<Rating>> {
        @Nonnull
        @Override
        public Cursor<Rating> apply(Pair<Long, IntList> input) {
            Preconditions.checkNotNull(input, "input entry");
            return Cursors.wrap(getRatingList(input.getRight()));
        }
    }

    private class ItemEntryTransformer implements Function<Pair<Long, IntList>, ItemEventCollection<Rating>> {
        @Nonnull
        @Override
        public ItemEventCollection<Rating> apply(Pair<Long, IntList> input) {
            return new BinaryItemCollection(input.getLeft(), getRatingList(input.getRight()));
        }
    }

    private class UserEntryTransformer implements Function<Pair<Long, IntList>, UserHistory<Rating>> {
        @Nonnull
        @Override
        public UserHistory<Rating> apply(Pair<Long, IntList> input) {
            return new BinaryUserHistory(input.getLeft(), getRatingList(input.getRight()));
        }
    }

    public static class Loader implements Provider<BinaryRatingDAO>, Serializable {
        public static final long serialVersionUID = 1L;

        private final File dataFile;

        @Inject
        public Loader(@BinaryRatingFile File file) {
            dataFile = file;
        }

        @Override
        public BinaryRatingDAO get() {
            try {
                return open(dataFile);
            } catch (IOException e) {
                throw new RuntimeException("cannot open rating file", e);
            }
        }
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private BinaryHeader header;
        private ByteBuffer ratingData;
        private BinaryIndexTable userTable;
        private BinaryIndexTable itemTable;

        public SerialProxy(BinaryHeader hdr, ByteBuffer ratings, BinaryIndexTable users, BinaryIndexTable items) {
            header = hdr;
            ratingData = ratings.duplicate();
            userTable = users;
            itemTable = items;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            byte[] headerBytes = new byte[BinaryHeader.HEADER_SIZE];
            ByteBuffer headBuffer = ByteBuffer.wrap(headerBytes);
            header.render(headBuffer);
            headBuffer.flip();
            out.writeInt(BinaryHeader.HEADER_SIZE);
            out.write(headerBytes);
            out.writeObject(userTable);
            out.writeObject(itemTable);

            // TODO Write this with a compound file
            ByteBuffer write = ratingData.duplicate();
            write.clear();
            out.writeInt(write.limit());
            byte[] buf = new byte[4096];
            while (write.hasRemaining()) {
                final int n = Math.min(4096, write.remaining());
                write.get(buf, 0, n);
                out.write(buf, 0, n);
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            int headSize = in.readInt();
            if (headSize != BinaryHeader.HEADER_SIZE) {
                throw new InvalidObjectException("incorrect header size");
            }
            byte[] headerBytes = new byte[BinaryHeader.HEADER_SIZE];
            int nbs = in.read(headerBytes);
            if (nbs != headSize) {
                throw new InvalidObjectException("not enough bytes for header");
            }
            ByteBuffer headBuf = ByteBuffer.wrap(headerBytes);
            header = BinaryHeader.fromHeader(headBuf);

            userTable = (BinaryIndexTable) in.readObject();
            itemTable = (BinaryIndexTable) in.readObject();

            int dataLength = in.readInt();
            byte[] buf = new byte[4096];
            ByteBuffer data = ByteBuffer.allocateDirect(dataLength);
            assert data.position() == 0;
            assert data.limit() == dataLength;
            while (data.hasRemaining()) {
                final int n = Math.min(4096, data.remaining());
                int read = in.read(buf, 0, n);
                if (read < 0) {
                    throw new InvalidObjectException("unexpected EOF");
                }
                data.put(buf, 0, read);
            }
            data.clear();
            ratingData = data;
        }

        private Object readResolve() throws ObjectStreamException {
            return new BinaryRatingDAO(null, header, ratingData, userTable, itemTable);
        }
    }
}
