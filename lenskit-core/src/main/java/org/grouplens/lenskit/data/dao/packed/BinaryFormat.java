package org.grouplens.lenskit.data.dao.packed;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.RatingBuilder;
import org.grouplens.lenskit.data.pref.Preference;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utilities for making the binary format.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class BinaryFormat {
    public static final byte[] HEADER_MAGIC = "LK".getBytes(Charsets.US_ASCII);
    static final int INT_SIZE = 4;
    static final int LONG_SIZE = 8;
    static final int DOUBLE_SIZE = 8;

    private final EnumSet<BinaryFormatFlag> formatFlags;
    private final boolean includeTimestamps;

    public BinaryFormat(EnumSet<BinaryFormatFlag> flags) {
        formatFlags = EnumSet.copyOf(flags);
        includeTimestamps = flags.contains(BinaryFormatFlag.TIMESTAMPS);
    }

    /**
     * Create a new binary format with some flags.
     * @param flags The flags.
     * @return The new binary format.
     */
    public static BinaryFormat create(BinaryFormatFlag... flags) {
        return new BinaryFormat(BinaryFormatFlag.makeSet(flags));
    }

    public static BinaryFormat fromFlags(short flagWord) {
        EnumSet<BinaryFormatFlag> flags;
        switch (flagWord) {
        case 0:
            flags = EnumSet.noneOf(BinaryFormatFlag.class);
            break;
        case 1:
            flags = EnumSet.of(BinaryFormatFlag.TIMESTAMPS);
            break;
        default:
            throw new IllegalArgumentException("unparsable flag word " + flagWord);
        }
        return new BinaryFormat(flags);
    }

    public boolean hasTimestamps() {
        return includeTimestamps;
    }

    public Set<BinaryFormatFlag> getFlags() {
        return Collections.unmodifiableSet(formatFlags);
    }

    public short getFlagWord() {
        short word = 0;
        for (BinaryFormatFlag flag: formatFlags) {
            word |= 1 << flag.ordinal();
        }
        return word;
    }

    public int getRatingSize() {
        int size = 2 * LONG_SIZE + DOUBLE_SIZE;
        if (hasTimestamps()) {
            size += LONG_SIZE;
        }
        return size;
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
