package org.grouplens.lenskit.data.dao.packed;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryHeader {
    public static final int HEADER_SIZE = BinaryFormat.INT_SIZE * 4;

    private final BinaryFormat format;
    private final int ratingCount;
    private final int userCount;
    private final int itemCount;

    BinaryHeader(BinaryFormat fmt, int nratings, int nusers, int nitems) {
        format = fmt;
        ratingCount = nratings;
        userCount = nusers;
        itemCount = nitems;
    }

    /**
     * Parse a header from a byte buffer.
     * @param buf The byte buffer.
     * @return The header.
     */
    public static BinaryHeader fromHeader(ByteBuffer buf) {
        Preconditions.checkArgument(buf.remaining() >= HEADER_SIZE,
                                    "buffer not large enough");
        byte[] magic = new byte[2];
        buf.get(magic);
        if (!Arrays.equals(magic, BinaryFormat.HEADER_MAGIC)) {
            throw new IllegalArgumentException("invalid magic");
        }
        short word = buf.getShort();
        BinaryFormat format = BinaryFormat.fromFlags(word);
        int nratings = buf.getInt();
        int nusers = buf.getInt();
        int nitems = buf.getInt();
        return new BinaryHeader(format, nratings, nusers, nitems);
    }

    /**
     * Parse a header from a channel.
     * @param chan The channel. The position is advanced by the header size, if the read is
     *             successful.
     * @return The header.
     */
    public static BinaryHeader read(ReadableByteChannel chan) throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(HEADER_SIZE);
        BinaryUtils.readBuffer(chan, buf);
        buf.flip();
        try {
            return fromHeader(buf);
        } catch (IllegalArgumentException ex) {
            throw new IOException("invalid file header", ex);
        }
    }

    /**
     * Create a new binary header.
     * @param fmt The format.
     * @param nratings The rating count.
     * @param nusers The user count.
     * @param nitems The item count.
     * @return The binary header.
     */
    public static BinaryHeader create(BinaryFormat fmt, int nratings, int nusers, int nitems) {
        return new BinaryHeader(fmt, nratings, nusers, nitems);
    }

    /**
     * Render this header to a byte buffer.
     * @param buf The target buffer.
     */
    public void render(ByteBuffer buf) {
        buf.put(BinaryFormat.HEADER_MAGIC);
        buf.putShort(format.getFlagWord());
        buf.putInt(ratingCount);
        buf.putInt(userCount);
        buf.putInt(itemCount);
    }

    public BinaryFormat getFormat() {
        return format;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getRatingDataSize() {
        return getRatingCount() * format.getRatingSize();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BinaryHeader[")
          .append(format)
          .append(", n=")
          .append(ratingCount)
          .append(", u=")
          .append(userCount)
          .append(", i=")
          .append(itemCount)
          .append("]");
        return sb.toString();
    }
}
