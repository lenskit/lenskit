/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * File utilities for LensKit. Called LKFileUtils to avoid conflict with FileUtils
 * classes that may be imported from other packages such as Guava, Plexus, or Commons.
 *
 * @author Michael Ekstrand
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
     */
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
        InputStream istream = new FileInputStream(file);
        try {
            InputStream wrapped = istream;
            switch (compression) {
                case GZIP:
                    wrapped = new GZIPInputStream(istream);
                    break;
                case AUTO:
                    if (isCompressed(file)) {
                        wrapped = new GZIPInputStream(istream);
                    }
                    break;
                default:
                    break;
            }
            return new InputStreamReader(wrapped, charset);
        } catch (RuntimeException e) {
            close(istream);
            throw e;
        } catch (IOException e) {
            close(istream);
            throw e;
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
        OutputStream ostream = new FileOutputStream(file);
        try {
            OutputStream wrapped = ostream;
            switch (compression) {
                case GZIP:
                    wrapped = new GZIPOutputStream(ostream);
                    break;
                case AUTO:
                    if (isCompressed(file)) {
                        wrapped = new GZIPOutputStream(ostream);
                    }
                    break;
                default:
                    break;
            }
            return new OutputStreamWriter(wrapped, charset);
        } catch (RuntimeException e) {
            close(ostream);
            throw e;
        } catch (IOException e) {
            close(ostream);
            throw e;
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
     * Close a set of closeable objects, swallowing and logging all exceptions.
     *
     * @param log     The logger to which to report errors.
     * @param toClose The objects to close.
     * @return {@code true} if all objects closed cleanly; {@code false} if some objects
     *         failed when closing.
     */
    public static boolean close(Logger log, Closeable... toClose) {
        boolean success = true;
        for (Closeable c : toClose) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    String msg = String.format("error closing %s: %s", c, e);
                    log.error(msg, e);
                    success = false;
                } catch (RuntimeException e) {
                    String msg = String.format("error closing %s: %s", c, e);
                    log.error(msg, e);
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * Close a group of objects, using a default logger.
     *
     * @param toClose The objects to close.
     * @return {@code true} if all objects closed successfully.
     * @see #close(Logger, Closeable...)
     */
    public static boolean close(Closeable... toClose) {
        return close(logger, toClose);
    }
}
