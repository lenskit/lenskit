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

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.event.MutableRating;
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
    private final BinaryFormat format;
    private ByteBuffer ratingBuffer;
    private long lastTimestamp;
    private boolean needsSorting;
    private int index;

    /**
     * Create a new binary rating packer.
     * @param file The output file.
     *
     * @throws IOException The output exception.
     */
    BinaryRatingPacker(File file, EnumSet<BinaryFormatFlag> flags) throws IOException {
        format = BinaryFormat.create(flags);
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
        for (Rating r: CollectionUtils.fast(ratings)) {
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
        channel.position(0);
        ByteBuffer buf = ByteBuffer.allocateDirect(BinaryHeader.HEADER_SIZE);
        BinaryHeader header = BinaryHeader.create(format, index, userMap.size(), itemMap.size());
        header.render(buf);
        buf.flip();
        BinaryUtils.writeBuffer(channel, buf);
    }

    /**
     * Write a user or item index to the file.
     * @param map The index to write.
     */
    private void writeIndex(Long2ObjectMap<IntList> map) throws IOException {
        LongSortedSet keys = LongUtils.packedSet(map.keySet());
        // allocate header buffer
        int headerSize = keys.size() * (8 + 4 + 4);
        ByteBuffer headerBuf = ByteBuffer.allocateDirect(headerSize);
        // temporary buffer for each entry
        ByteBuffer buf = null;

        // save the position
        long indexPosition = channel.position();
        // and skip the header table for now
        channel.position(indexPosition + headerSize);

        // track the offset into the index entries
        int offset = 0;

        LongIterator iter = keys.iterator();
        while (iter.hasNext()) {
            final long key = iter.nextLong();
            IntList indexes = map.get(key);

            // write into the header buffer
            headerBuf.putLong(key);
            headerBuf.putInt(offset);
            headerBuf.putInt(indexes.size());
            offset += indexes.size();

            // write the index entries
            int size = indexes.size() * 4;
            if (buf == null || buf.capacity() < size) {
                buf = ByteBuffer.allocateDirect(size);
            }
            IntIterator iiter = indexes.iterator();
            while (iiter.hasNext()) {
                buf.putInt(iiter.nextInt());
            }
            buf.flip();
            BinaryUtils.writeBuffer(channel, buf);
            buf.clear();
        }

        long endOfTable = channel.position();
        // write back the header buffer
        channel.position(indexPosition);
        headerBuf.flip();
        BinaryUtils.writeBuffer(channel, headerBuf);
        channel.position(endOfTable);
    }

    /**
     * Sort the ratings.
     */
    private void sortRatings() {
        Arrays.quickSort(0, index, new SortComparator(), new SortSwapper());
    }

    private long ratingPos(int idx) {
        long offset = format.getHeaderSize();
        return offset + idx * format.getRatingSize();
    }

    private class SortComparator extends AbstractIntComparator {
        private ByteBuffer buf = ByteBuffer.allocateDirect(format.getRatingSize());
        private MutableRating r1 = new MutableRating();
        private MutableRating r2 = new MutableRating();

        @Override
        public int compare(int i1, int i2) {
            try {
                BinaryUtils.readBuffer(channel, buf, ratingPos(i1));
                buf.flip();
                format.readRating(buf, r1);
                buf.clear();

                BinaryUtils.readBuffer(channel, buf, ratingPos(i2));
                buf.flip();
                format.readRating(buf, r2);
                buf.clear();
            } catch (IOException ex) {
                throw new RuntimeException("I/O error while sorting", ex);
            }
            return Events.TIMESTAMP_COMPARATOR.compare(r1, r2);
        }
    }

    private class SortSwapper implements Swapper {
        private ByteBuffer b1 = ByteBuffer.allocateDirect(format.getRatingSize());
        private ByteBuffer b2 = ByteBuffer.allocateDirect(format.getRatingSize());

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
            } catch (IOException ex) {
                throw new RuntimeException("I/O error while sorting", ex);
            }
        }
    }
}
