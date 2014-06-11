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

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.pref.Preference;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utilities for making the binary format.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
final class BinaryFormat {
    public static final byte[] HEADER_MAGIC = "LK".getBytes(Charsets.US_ASCII);
    static final int INT_SIZE = 4;
    static final int LONG_SIZE = 8;
    static final int DOUBLE_SIZE = 8;

    private final EnumSet<PackHeaderFlag> formatFlags;
    private final boolean includeTimestamps;
    private final int ratingSize;

    private BinaryFormat(Set<PackHeaderFlag> flags) {
        formatFlags = EnumSet.copyOf(flags);
        includeTimestamps = flags.contains(PackHeaderFlag.TIMESTAMPS);

        int rsz = 2 * LONG_SIZE + DOUBLE_SIZE;
        if (hasTimestamps()) {
            rsz += LONG_SIZE;
        }
        ratingSize = rsz;
    }

    /**
     * Create a new binary format with some flags.
     * @param flags The flags.
     * @return The new binary format.
     */
    public static BinaryFormat create(PackHeaderFlag... flags) {
        return new BinaryFormat(Sets.newEnumSet(Arrays.asList(flags),  PackHeaderFlag.class));
    }

    /**
     * Create a new binary format with some externally-facing flags and the default header
     * settings.
     * @param flags The format flags.
     * @return A new format.
     */
    public static BinaryFormat create(Set<BinaryFormatFlag> flags) {
        return new BinaryFormat(PackHeaderFlag.fromFormatFlags(flags));
    }

    public static BinaryFormat fromFlags(short flagWord) {
        EnumSet<PackHeaderFlag> flags = EnumSet.noneOf(PackHeaderFlag.class);

        int word = ((int) flagWord) & 0x0000FFFF;
        int n = 0;
        while (word != 0 && n < PackHeaderFlag.values().length) {
            if ((word & 0x01) != 0) {
                flags.add(PackHeaderFlag.values()[n]);
            }
            n++;
            word = word >>> 1;
        }

        if (word != 0) {
            throw new IllegalArgumentException(String.format("unparseable flag word %x", flagWord));
        }

        return new BinaryFormat(flags);
    }

    public boolean hasTimestamps() {
        return includeTimestamps;
    }

    public Set<PackHeaderFlag> getFlags() {
        return Sets.newEnumSet(formatFlags, PackHeaderFlag.class);
    }

    public short getFlagWord() {
        short word = 0;
        for (PackHeaderFlag flag: formatFlags) {
            word |= 1 << flag.ordinal();
        }
        return word;
    }

    public int getRatingSize() {
        return ratingSize;
    }

    public int getHeaderSize() {
        return BinaryHeader.HEADER_SIZE;
    }

    /**
     * Render a rating to a byte buffer.
     * @param rating The rating.
     * @param buf The buffer.
     */
    public void renderRating(Rating rating, ByteBuffer buf) {
        buf.putLong(rating.getUserId());
        buf.putLong(rating.getItemId());
        Preference pref = rating.getPreference();
        if (pref == null) {
            buf.putDouble(Double.NaN);
        } else {
            buf.putDouble(pref.getValue());
        }
        if (hasTimestamps()) {
            buf.putLong(rating.getTimestamp());
        }
    }

    /**
     * Read a rating from a buffer.
     * @param buf The buffer to read.
     * @return The rating.
     */
    public Rating readRating(ByteBuffer buf) {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(buf.getLong());
        rb.setItemId(buf.getLong());
        double rating = buf.getDouble();
        if (!Double.isNaN(rating)) {
            rb.setRating(rating);
        }
        if (hasTimestamps()) {
            rb.setTimestamp(buf.getLong());
        }
        return rb.build();
    }

    /**
     * Read a rating from a buffer into a mutable rating.
     * @param buf The buffer to read.
     * @param rating The rating to populate.
     */
    public void readRating(ByteBuffer buf, MutableRating rating) {
        rating.setUserId(buf.getLong());
        rating.setItemId(buf.getLong());
        rating.setRating(buf.getDouble());
        if (hasTimestamps()) {
            rating.setTimestamp(buf.getLong());
        }
    }

    /**
     * Render a user or item index entry.
     * @param key The user or item ID.
     * @param positions The list of indexes to record.
     * @param buf The byte buffer to receive the entry.
     */
    public void renderIndexEntry(long key, IntList positions, ByteBuffer buf) {
        buf.putLong(key);
        buf.putInt(positions.size());
        IntIterator iter = positions.iterator();
        while (iter.hasNext()) {
            buf.putInt(iter.nextInt());
        }
    }

    @Override
    public String toString() {
        return "BinFormat" + formatFlags.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinaryFormat that = (BinaryFormat) o;

        if (formatFlags != null ? !formatFlags.equals(that.formatFlags) : that.formatFlags != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return formatFlags != null ? formatFlags.hashCode() : 0;
    }
}
