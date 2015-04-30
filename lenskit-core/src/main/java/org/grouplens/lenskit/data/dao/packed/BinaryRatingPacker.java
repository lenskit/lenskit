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

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.event.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.EnumSet;
import java.util.Set;

/**
 * Creates rating pack files for the {@link BinaryRatingDAO}.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class BinaryRatingPacker implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(BinaryRatingPacker.class);
    private final File outputFile;
    private RandomAccessFile output;
    private FileChannel channel;
    private Long2ObjectMap<IntList> userMap;
    private Long2ObjectMap<IntList> itemMap;
    private BinaryFormat format;
    private ByteBuffer ratingBuffer;
    private long lastTimestamp;
    private boolean needsSorting;
    private int index;
    /**
     * Map insertion-order indexes to post-sorting indexes.
     */
    private int[] translationMap;

    /**
     * Create a new binary rating packer.
     * @param file The output file.
     *
     * @throws IOException The output exception.
     */
    BinaryRatingPacker(File file, EnumSet<BinaryFormatFlag> flags) throws IOException {
        format = BinaryFormat.createWithFlags(PackHeaderFlag.fromFormatFlags(flags));
        outputFile = file;

        logger.debug("opening binary pack file {}", outputFile);
        output = new RandomAccessFile(file, "rw");
        channel = output.getChannel();

        userMap = new Long2ObjectOpenHashMap<IntList>();
        itemMap = new Long2ObjectOpenHashMap<IntList>();

        lastTimestamp = Long.MIN_VALUE;
        needsSorting = false;
        index = 0;

        // skip the header
        channel.position(BinaryHeader.HEADER_SIZE);

        ratingBuffer = ByteBuffer.allocateDirect(format.getRatingSize());
    }

    /**
     * Open a new binary rating packer.
     * @param file The output file.
     * @param flags The flags to use when creating the file.
     *
     * @throws IOException The output exception.
     */
    public static BinaryRatingPacker open(File file, BinaryFormatFlag... flags) throws IOException {
        return open(file, BinaryFormatFlag.makeSet(flags));
    }

    /**
     * Open a new binary rating packer.
     * @param file The output file.
     * @param flags The flags to use when creating the file.
     *
     * @throws IOException The output exception.
     */
    public static BinaryRatingPacker open(File file, EnumSet<BinaryFormatFlag> flags) throws IOException {
        return new BinaryRatingPacker(file, flags);
    }

    public void writeRating(Rating rating) throws IOException {
        // the buffer should already be clear
        assert ratingBuffer.position() == 0;
        assert ratingBuffer.limit() == ratingBuffer.capacity();

        checkUpgrade(rating.getUserId(), rating.getItemId());

        // and use it
        format.renderRating(rating, ratingBuffer);
        ratingBuffer.flip();
        BinaryUtils.writeBuffer(channel, ratingBuffer);
        ratingBuffer.clear();

        saveIndex(userMap, rating.getUserId(), index);
        saveIndex(itemMap, rating.getItemId(), index);
        index += 1;

        if (format.hasTimestamps()) {
            long ts = rating.getTimestamp();
            // did this timestamp send us backwards?
            if (ts < lastTimestamp && !needsSorting) {
                logger.debug("found out-of-order timestamps, activating sorting");
                needsSorting = true;
            }
            lastTimestamp = ts;
        }
    }

    public void writeRatings(Iterable<? extends Rating> ratings) throws IOException {
        for (Rating r: ratings) {
            writeRating(r);
        }
    }

    public int getRatingCount() {
        return index;
    }

    private void saveIndex(Long2ObjectMap<IntList> map, long key, int index) {
        IntList list = map.get(key);
        if (list == null) {
            list = new IntArrayList();
            map.put(key, list);
        }
        list.add(index);
    }

    @Override
    public void close() throws IOException {
        try {
            logger.debug("closing binary pack file {}", outputFile);
            if (needsSorting) {
                logger.info("sorting {} ratings", index);
                sortRatings();
            }
            writeIndex(userMap);
            writeIndex(itemMap);
            writeHeader();
            channel.force(true);
        } finally {
            channel.close();
            output.close();
        }
    }

    private void writeHeader() throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(BinaryHeader.HEADER_SIZE);
        BinaryHeader header = BinaryHeader.create(format, index, userMap.size(), itemMap.size());
        header.render(buf);
        buf.flip();
        BinaryUtils.writeBuffer(channel, buf, 0);
    }

    /**
     * Write a user or item index to the file.
     * @param map The index to write.
     */
    private void writeIndex(Long2ObjectMap<IntList> map) throws IOException {
        LongSortedSet keys = LongUtils.packedSet(map.keySet());

        BinaryIndexTableWriter tableWriter =
                BinaryIndexTableWriter.create(format, channel, keys.size());

        LongIterator iter = keys.iterator();
        while (iter.hasNext()) {
            final long key = iter.nextLong();
            int[] indexes = map.get(key).toIntArray();

            if (translationMap != null) {
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i] = translationMap[indexes[i]];
                }
                java.util.Arrays.sort(indexes);
            }

            logger.debug("writing {} indices for id {}", key, indexes.length);
            tableWriter.writeEntry(key, indexes);
        }
    }

    private void checkUpgrade(long uid, long iid) throws IOException {
        Set<PackHeaderFlag> toRemove = null;
        if (!format.userIdIsValid(uid)) {
            assert format.hasCompactUsers();
            toRemove = EnumSet.of(PackHeaderFlag.COMPACT_USERS);
        }
        if (!format.itemIdIsValid(iid)) {
            assert format.hasCompactItems();
            if (toRemove == null) {
                toRemove = EnumSet.of(PackHeaderFlag.COMPACT_ITEMS);
            } else {
                toRemove.add(PackHeaderFlag.COMPACT_ITEMS);
            }
        }

        if (toRemove != null) {
            Set<PackHeaderFlag> newFlags = EnumSet.copyOf(format.getFlags());
            newFlags.removeAll(toRemove);
            BinaryFormat newFormat = BinaryFormat.createWithFlags(newFlags);
            if (newFormat != format) {
                upgradeRatings(newFormat);
            }
        }
    }

    private void upgradeRatings(BinaryFormat newFormat) throws IOException {
        Preconditions.checkArgument(newFormat.getRatingSize() > format.getRatingSize(),
                                    "new format is not wider than old");
        logger.info("upgrading {} ratings from {} to {}", index, format, newFormat);

        ByteBuffer oldBuffer = ByteBuffer.allocateDirect(format.getRatingSize());
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(newFormat.getRatingSize());

        long oldPos = BinaryHeader.HEADER_SIZE + index * format.getRatingSize();
        Preconditions.checkState(channel.position() == oldPos,
                                 "channel is at the wrong position");
        long newPos = BinaryHeader.HEADER_SIZE + index * newFormat.getRatingSize();
        channel.position(newPos);
        // loop backwards, coping each rating to later in the file
        for (int i = index - 1; i >= 0; i--) {
            oldPos -= format.getRatingSize();
            newPos -= newFormat.getRatingSize();

            // read the old rating
            BinaryUtils.readBuffer(channel, oldBuffer, oldPos);
            oldBuffer.flip();
            Rating scratch = format.readRating(oldBuffer);
            oldBuffer.clear();

            // write the new rating
            newFormat.renderRating(scratch, newBuffer);
            newBuffer.flip();
            BinaryUtils.writeBuffer(channel, newBuffer, newPos);
            newBuffer.clear();
        }
        assert oldPos == BinaryHeader.HEADER_SIZE;
        assert newPos == BinaryHeader.HEADER_SIZE;
        format = newFormat;
        ratingBuffer = ByteBuffer.allocateDirect(newFormat.getRatingSize());
    }

    /**
     * Sort the ratings.
     */
    private void sortRatings() {
        if (translationMap != null) {
            throw new IllegalStateException("sort already invoked");
        }
        // the inv map will map post-sort indexes to original indexes
        int[] invMap = new int[index];
        for (int i = index - 1; i >= 0; i--) {
            invMap[i] = i;
        }

        Arrays.quickSort(0, index, new SortComparator(), new SortSwapper(invMap));

        // invert the inv map, so we map pre-sort to post-sort indexes
        translationMap = new int[index];
        for (int i = 0; i < invMap.length; i++) {
            translationMap[invMap[i]] = i;
        }
    }

    private long ratingPos(int idx) {
        long offset = format.getHeaderSize();
        return offset + idx * (long) format.getRatingSize();
    }

    private class SortComparator extends AbstractIntComparator {
        private ByteBuffer buf = ByteBuffer.allocateDirect(format.getRatingSize());
        @Override
        public int compare(int i1, int i2) {
            if (translationMap != null) {
                i1 = translationMap[i1];
                i2 = translationMap[i2];
            }
            try {
                BinaryUtils.readBuffer(channel, buf, ratingPos(i1));
                buf.flip();
                Rating r1 = format.readRating(buf);
                buf.clear();

                BinaryUtils.readBuffer(channel, buf, ratingPos(i2));
                buf.flip();
                Rating r2 = format.readRating(buf);
                buf.clear();

                return Events.TIMESTAMP_COMPARATOR.compare(r1, r2);
            } catch (IOException ex) {
                throw new RuntimeException("I/O error while sorting", ex);
            }
        }
    }

    private class SortSwapper implements Swapper {
        private final int[] inverseTranslationMap;
        private ByteBuffer b1 = ByteBuffer.allocateDirect(format.getRatingSize());
        private ByteBuffer b2 = ByteBuffer.allocateDirect(format.getRatingSize());

        SortSwapper(int[] map) {
            inverseTranslationMap = map;
        }

        @Override
        public void swap(int i1, int i2) {
            long p1 = ratingPos(i1);
            long p2 = ratingPos(i2);
            try {
                BinaryUtils.readBuffer(channel, b1, p1);
                b1.flip();
                BinaryUtils.readBuffer(channel, b2, p2);
                b2.flip();

                BinaryUtils.writeBuffer(channel, b1, p2);
                BinaryUtils.writeBuffer(channel, b2, p1);

                b1.clear();
                b2.clear();

                int j = inverseTranslationMap[i1];
                inverseTranslationMap[i1] = inverseTranslationMap[i2];
                inverseTranslationMap[i2] = j;
            } catch (IOException ex) {
                throw new RuntimeException("I/O error while sorting", ex);
            }
        }
    }
}
