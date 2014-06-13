/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.pref.Preference;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
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
    private final boolean compactItems, compactUsers;
    private final int ratingSize;

    private BinaryFormat(Set<PackHeaderFlag> flags) {
        formatFlags = EnumSet.copyOf(flags);
        includeTimestamps = flags.contains(PackHeaderFlag.TIMESTAMPS);
        compactItems = flags.contains(PackHeaderFlag.COMPACT_ITEMS);
        compactUsers = flags.contains(PackHeaderFlag.COMPACT_USERS);

        int rsz = DOUBLE_SIZE;
        rsz += compactItems ? INT_SIZE : LONG_SIZE;
        rsz += compactUsers ? INT_SIZE : LONG_SIZE;
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
     * Create a new binary format with some flags.
     * @param flags The flags.
     * @return The new binary format.
     */
    public static BinaryFormat createWithFlags(Set<PackHeaderFlag> flags) {
        return new BinaryFormat(Sets.newEnumSet(flags,  PackHeaderFlag.class));
    }

    /**
     * Create a new binary format with some externally-facing flags and the default header
     * settings.
     * @param flags The format flags.
     * @return A new format.
     */
    @Deprecated
    public static BinaryFormat create(Set<BinaryFormatFlag> flags) {
        Set<PackHeaderFlag> hflags = PackHeaderFlag.fromFormatFlags(flags);
        return new BinaryFormat(hflags);
    }

    public static BinaryFormat fromFlags(short flagWord) {
        EnumSet<PackHeaderFlag> flags = PackHeaderFlag.unpackWord(flagWord);

        return new BinaryFormat(flags);
    }

    public boolean hasTimestamps() {
        return includeTimestamps;
    }

    public boolean hasCompactItems() {
        return compactItems;
    }

    public boolean hasCompactUsers() {
        return compactUsers;
    }

    public boolean isCompact() {
        return compactUsers || compactItems;
    }

    public Set<PackHeaderFlag> getFlags() {
        return Collections.unmodifiableSet(formatFlags);
    }

    public short getFlagWord() {
        return PackHeaderFlag.packWord(formatFlags);
    }

    public int getRatingSize() {
        return ratingSize;
    }

    public int getHeaderSize() {
        return BinaryHeader.HEADER_SIZE;
    }

    static long readId(ByteBuffer buf, boolean compact) {
        if (compact) {
            return buf.getInt();
        } else {
            return buf.getLong();
        }
    }

    static void writeId(ByteBuffer buf, long id, boolean compact) {
        if (compact) {
            assert id >= Integer.MIN_VALUE && id <= Integer.MAX_VALUE;
            buf.putInt((int) id);
        } else {
            buf.putLong(id);
        }
    }

    public int getUserIdSize() {
        return compactUsers ? INT_SIZE : LONG_SIZE;
    }

    public int getItemIdSize() {
        return compactUsers ? INT_SIZE : LONG_SIZE;
    }

    public boolean userIdIsValid(long id) {
        if (compactUsers) {
            return id >= Integer.MIN_VALUE && id <= Integer.MAX_VALUE;
        } else {
            return true;
        }
    }

    public boolean itemIdIsValid(long id) {
        if (compactItems) {
            return id >= Integer.MIN_VALUE && id <= Integer.MAX_VALUE;
        } else {
            return true;
        }
    }

    public long readUserId(ByteBuffer buf) {
        return readId(buf, compactUsers);
    }

    public long readItemId(ByteBuffer buf) {
        return readId(buf, compactItems);
    }

    public void writeUserId(ByteBuffer buf, long id) {
        writeId(buf, id, compactUsers);
    }

    public void writeItemId(ByteBuffer buf, long id) {
        writeId(buf, id, compactItems);
    }

    /**
     * Render a rating to a byte buffer.
     * @param rating The rating.
     * @param buf The buffer.
     */
    public void renderRating(Rating rating, ByteBuffer buf) {
        writeUserId(buf, rating.getUserId());
        writeItemId(buf, rating.getItemId());
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
        rb.setUserId(readUserId(buf));
        rb.setItemId(readItemId(buf));
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
        rating.setUserId(readUserId(buf));
        rating.setItemId(readItemId(buf));
        rating.setRating(buf.getDouble());
        if (hasTimestamps()) {
            rating.setTimestamp(buf.getLong());
        } else {
            rating.setTimestamp(-1);
        }
    }

    public int indexTableEntrySize() {
        return BinaryIndexTable.TABLE_ENTRY_SIZE;
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
