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
package org.grouplens.lenskit.util.io;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillCloseWhenClosed;
import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * File utilities for LensKit. Called LKFileUtils to avoid conflict with FileUtils
 * classes that may be imported from other packages such as Guava, Plexus, or Commons.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public final class LKFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(LKFileUtils.class);

    private LKFileUtils() {
    }

    /**
     * Query whether this filename represents a compressed file. It just looks at
     * the name to see if it ends in “.gz”.
     *
     * @param file The file to query.
     * @return {@code true} if the file name ends in “.gz”.
     * @deprecated Use {@link CompressionMode} or commons-compress facilities instead.
     */
    @Deprecated
    public static boolean isCompressed(File file) {
        return file.getName().endsWith(".gz");
    }

    /**
     * Open a file for input, optionally compressed.
     *
     * @param file        The file to open.
     * @param charset     The character set to use.
     * @param compression Whether to compress the file.
     * @return A reader opened on the file.
     * @throws IOException if there is an error opening the file.
     */
    public static Reader openInput(File file, Charset charset, CompressionMode compression) throws IOException {
        CompressionMode effComp = compression.getEffectiveCompressionMode(file.getName());
        InputStream istream = new FileInputStream(file);
        try {
            InputStream wrapped = effComp.wrapInput(istream);
            return new InputStreamReader(wrapped, charset);
        } catch (Exception ex) {
            Closeables.close(istream, true);
            Throwables.propagateIfPossible(ex, IOException.class);
            throw Throwables.propagate(ex);
        }
    }

    /**
     * Open a file for input with the default charset.
     *
     * @param file        The file to open.
     * @param compression The compression mode.
     * @return A reader opened on the file.
     * @throws IOException if there was an error opening the file.
     * @see #openInput(java.io.File, Charset, CompressionMode)
     */
    public static Reader openInput(File file, CompressionMode compression) throws IOException {
        return openInput(file, Charset.defaultCharset(), compression);
    }

    /**
     * Open a reader with automatic compression and the default character set.
     *
     * @param file The file to open.
     * @return A reader opened on the input file.
     * @throws IOException if there is an error opening the file.
     * @see #openInput(File, Charset, CompressionMode)
     * @see CompressionMode#AUTO
     * @see Charset#defaultCharset()
     */
    @SuppressWarnings("unused")
    public static Reader openInput(File file) throws IOException {
        return openInput(file, Charset.defaultCharset(), CompressionMode.AUTO);
    }

    /**
     * Open a file for input, optionally compressed.
     *
     * @param file        The file to open.
     * @param charset     The character set to use.
     * @param compression Whether to compress the file.
     * @return A reader opened on the file.
     * @throws IOException if there is an error opening the file.
     */
    public static Writer openOutput(File file, Charset charset, CompressionMode compression) throws IOException {
        CompressionMode effComp = compression.getEffectiveCompressionMode(file.getName());
        OutputStream ostream = new FileOutputStream(file);
        try {
            OutputStream wrapped = effComp.wrapOutput(ostream);
            return new OutputStreamWriter(wrapped, charset);
        } catch (Exception ex) {
            Closeables.close(ostream, true);
            Throwables.propagateIfPossible(ex, IOException.class);
            throw Throwables.propagate(ex);
        }
    }

    /**
     * Open a file for output with the default charset.
     *
     * @param file        The file to open.
     * @param compression The compression mode.
     * @return A writer opened on the file.
     * @throws IOException if there was an error opening the file.
     * @see #openInput(java.io.File, Charset, CompressionMode)
     */
    @SuppressWarnings("unused")
    public static Writer openOutput(File file, CompressionMode compression) throws IOException {
        return openOutput(file, Charset.defaultCharset(), compression);
    }

    /**
     * Open a reader with automatic compression inference.
     *
     * @param file The file to open.
     * @return A reader opened on the input file.
     * @throws IOException if there is an error opening the file.
     */
    @SuppressWarnings("unused")
    public static Writer openOutput(File file) throws IOException {
        return openOutput(file, Charset.defaultCharset(), CompressionMode.AUTO);
    }

    /**
     * Auto-detect whether a stream needs decompression.  Currently detects GZIP compression (using
     * the GZIP magic in the header).
     *
     * @param stream The stream to read.
     * @return A stream that will read from {@code stream}, decompressing if needed.  It may not be
     *         the same object as {@code stream}, even if no decompression is needed, as the input
     *         stream may be wrapped in a buffered stream for lookahead.
     */
    public static InputStream transparentlyDecompress(@WillCloseWhenClosed InputStream stream) throws IOException {
        InputStream buffered;
        // get a markable stream
        if (stream.markSupported()) {
            buffered = stream;
        } else {
            logger.debug("stream {} does not support mark, wrapping", stream);
            buffered = new BufferedInputStream(stream);
        }

        // read the first 2 bytes for GZIP magic
        buffered.mark(2);
        int b1 = buffered.read();
        if (b1 < 0) {
            buffered.reset();
            return buffered;
        }
        int b2 = buffered.read();
        if (b2 < 0) {
            buffered.reset();
            return buffered;
        }
        buffered.reset();

        // they're in little-endian order
        int magic = b1 | (b2 << 8);

        logger.debug(String.format("found magic %x", magic));
        if (magic == GZIPInputStream.GZIP_MAGIC) {
            logger.debug("stream is gzip-compressed, decompressing");
            return new GZIPInputStream(buffered);
        }

        return buffered;
    }

    /**
     * Read a list of long IDs from a file, one per line.
     * @param file The file to read.
     * @return A list of longs.
     */
    public static LongList readIdList(File file) throws IOException {
        LongList items = new LongArrayList();
        Closer closer = Closer.create();
        try {
            FileReader fread = closer.register(new FileReader(file));
            BufferedReader buf = closer.register(new BufferedReader(fread));
            String line;
            int lno = 0;
            while ((line = buf.readLine()) != null) {
                lno += 1;
                if (line.trim().isEmpty()) {
                    continue; // skip blank lines
                }
                long item;
                try {
                    item = Long.parseLong(line.trim());
                } catch (IllegalArgumentException ex) {
                    throw new IOException("invalid ID on " + file + " line " + lno + ": " + line);
                }
                items.add(item);
            }
        } catch (Throwable th) { // NOSONAR using a closer
            throw closer.rethrow(th);
        } finally {
            closer.close();
        }
        return items;
    }
}
