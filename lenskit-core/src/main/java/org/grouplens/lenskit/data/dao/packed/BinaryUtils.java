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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
     * Read a buffer completely from a position in a file.
     * @param chan The channel.
     * @param buf The buffer.
     * @param pos The position from which to read.
     * @throws java.io.IOException If an error occurs while read the buffer.
     */
    public static void readBuffer(FileChannel chan, ByteBuffer buf, long pos) throws IOException {
        long cpos = pos;
        while (buf.hasRemaining()) {
            cpos += chan.read(buf, cpos);
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

    /**
     * Write a buffer completely to a position in a file.
     * @param chan The channel.
     * @param buf The buffer.
     * @param pos The position from which to read.
     * @throws java.io.IOException If an error occurs while read the buffer.
     */
    public static void writeBuffer(FileChannel chan, ByteBuffer buf, long pos) throws IOException {
        long cpos = pos;
        while (buf.hasRemaining()) {
            cpos += chan.write(buf, cpos);
        }
    }
}
