package org.grouplens.lenskit.data.dao.packed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * DAO implementation using binary-packed data.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryRatingDAO implements EventDAO, UserEventDAO, ItemEventDAO, UserDAO, ItemDAO {
    private static final Logger logger = LoggerFactory.getLogger(BinaryRatingDAO.class);
    private final File backingFile;
    private final BinaryHeader header;
    private final MappedByteBuffer data;
    private final BinaryIndexTable userTable;
    private final BinaryIndexTable itemTable;

    @Inject
    public BinaryRatingDAO(@BinaryRatingFile File file) throws IOException {
        backingFile = file;
        FileInputStream input = new FileInputStream(file);
        try {
            FileChannel channel = input.getChannel();
            header = BinaryHeader.read(channel);
            logger.info("Loading DAO with {} ratings of {} items from {} users",
                        header.getRatingCount(), header.getItemCount(), header.getItemCount());

            data = channel.map(FileChannel.MapMode.READ_ONLY,
                               channel.position(), channel.size() - channel.position());

            ByteBuffer tableBuffer = data.duplicate();
            tableBuffer.position(header.getRatingDataSize());
            userTable = BinaryIndexTable.create(header.getUserCount(), tableBuffer);
            itemTable = BinaryIndexTable.create(header.getItemCount(), tableBuffer);
        } finally {
            input.close();
        }
    }

    private BinaryRatingList getRatingList() {
        return getRatingList(CollectionUtils.interval(0, header.getRatingCount()));
    }

    private BinaryRatingList getRatingList(IntList indexes) {
        return new BinaryRatingList(header.getFormat(), data, indexes);
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

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> getEventsForItem(long item) {
        return getEventsForItem(item, Event.class);
    }

    @Nullable
    @Override
    public <E extends Event> List<E> getEventsForItem(long item, Class<E> type) {
        IntList indx = itemTable.getEntry(item);
        if (indx == null) {
            return null;
        }

        if (!type.isAssignableFrom(Rating.class)) {
            return ImmutableList.of();
        }

        return (List<E>) getRatingList(indx);
    }

    @Nullable
    @Override
    public LongSet getUsersForItem(long item) {
        List<Rating> ratings = getEventsForItem(item, Rating.class);
        if (ratings == null) {
            return null;
        }

        LongSet users = new LongOpenHashSet(ratings.size());
        for (Rating rating: CollectionUtils.fast(ratings)) {
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
        return Cursors.wrap(Collections2.transform(userTable.entries(),
                                                   new UserEntryTransformer()));
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
        IntList indx = userTable.getEntry(user);
        if (indx == null) {
            return null;
        }

        if (!type.isAssignableFrom(Rating.class)) {
            return History.forUser(user);
        }

        return (UserHistory<E>) new BinaryUserHistory(user, getRatingList(indx));
    }

    private class EntryToCursorTransformer implements Function<Pair<Long, IntList>, Cursor<Rating>> {
        @Nullable
        @Override
        public Cursor<Rating> apply(@Nullable Pair<Long, IntList> input) {
            return Cursors.wrap(getRatingList(input.getRight()));
        }
    }

    private class UserEntryTransformer implements Function<Pair<Long, IntList>, UserHistory<Event>> {
        @Nullable
        @Override
        public UserHistory<Event> apply(@Nullable Pair<Long, IntList> input) {
            return History.<Event>forUser(input.getLeft(), getRatingList(input.getRight()));
        }
    }
}
