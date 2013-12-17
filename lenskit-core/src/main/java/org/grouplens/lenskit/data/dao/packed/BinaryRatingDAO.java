package org.grouplens.lenskit.data.dao.packed;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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

        switch (order) {
        case ANY:
        case TIMESTAMP:
            break;
        default:
            throw new UnsupportedOperationException("sorting not supported");
        }

        return (Cursor<E>) getRatingList().cursor();
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
