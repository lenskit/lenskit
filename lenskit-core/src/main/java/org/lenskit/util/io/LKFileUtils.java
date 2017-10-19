/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.io;

import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillCloseWhenClosed;
import java.io.*;
import java.net.URL;
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
        } catch (IOException ex) {
            try {
                ostream.close();
            } catch (IOException ex2) {
                ex.addSuppressed(ex2);
            }
            throw ex;
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
     * Create a file byte source, automatically decompressing based on file name.
     * @param file The file byte source.
     * @return The byte source, possibly decompressing.
     */
    public static ByteSource byteSource(File file) {
        return byteSource(file, CompressionMode.AUTO);
    }

    /**
     * Create a file byte source.
     * @param file The file containing data.
     * @param compression The compression mode.
     * @return The byte source, possibly decompressing.
     */
    public static ByteSource byteSource(File file, CompressionMode compression) {
        CompressionMode effectiveMode = compression.getEffectiveCompressionMode(file.getName());
        ByteSource source = Files.asByteSource(file);
        if (!effectiveMode.equals(CompressionMode.NONE)) {
            source = new CompressedByteSource(source, effectiveMode.getCompressorName());
        }
        return source;
    }

    /**
     * Create a URL-backed byte source.
     * @param url The URL of the byte source.
     * @param compression The compression mode.
     * @return The byte source, possibly decompressing.
     */
    public static ByteSource byteSource(URL url, CompressionMode compression) {
        CompressionMode effectiveMode = compression.getEffectiveCompressionMode(url.getPath());
        ByteSource source = Resources.asByteSource(url);
        if (!effectiveMode.equals(CompressionMode.NONE)) {
            source = new CompressedByteSource(source, effectiveMode.getCompressorName());
        }
        return source;
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
        try (FileReader fread = new FileReader(file);
             BufferedReader buf = new BufferedReader(fread)) {
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
        }
        return items;
    }

    /**
     * Get the basename of a file path, possibly without extension.
     * @param path The file path.
     * @param keepExt Whether to keep the extension.
     * @return The base name.
     */
    public static String basename(String path, boolean keepExt) {
        int idx = path.lastIndexOf(File.separatorChar);
        int idxUnix = path.lastIndexOf('/');
        if (idxUnix > idx) {
            idx = idxUnix;
        }
        String name = idx >= 0 ? path.substring(idx + 1) : path;
        if (!keepExt) {
            int eidx = name.lastIndexOf('.');
            if (eidx > 0) {
                name = name.substring(0, eidx);
            }
        }
        return name;
    }
}
