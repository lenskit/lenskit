package org.grouplens.lenskit.data.dao.packed;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class BinaryUtils {
    private BinaryUtils() {}

    /**
     * Read a buffer completely from a channel.
     * @param chan The channel.
     * @param buf The buffer.
     * @throws java.io.IOException If an error occurs while read the buffer.
     */
    public static void readBuffer(ReadableByteChannel chan, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            chan.read(buf);
        }
    }

    /**
     * Write a buffer completely to the channel.
     * @param buf The buffer.
     * @throws java.io.IOException If an error occurs while writing the buffer.
     */
    public static void writeBuffer(WritableByteChannel chan, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            chan.write(buf);
        }
    }
}
