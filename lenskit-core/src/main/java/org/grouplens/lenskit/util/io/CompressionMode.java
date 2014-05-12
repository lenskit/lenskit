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
package org.grouplens.lenskit.util.io;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.compressors.xz.XZUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum CompressionMode {
    /**
     * No compression.
     */
    NONE,

    /**
     * GZip compression.
     *
     * @see java.util.zip.GZIPInputStream
     * @see java.util.zip.GZIPOutputStream
     */
    GZIP(CompressorStreamFactory.GZIP),

    /**
     * XZ compression.
     */
    XZ(CompressorStreamFactory.XZ),

    /**
     * Automatically infer compression from file extension.
     */
    AUTO {
        @Override
        public CompressionMode getEffectiveCompressionMode(String filename) {
            if (GzipUtils.isCompressedFilename(filename)) {
                return GZIP;
            } else if (XZUtils.isCompressedFilename(filename)) {
                return XZ;
            } else {
                return NONE;
            }
        }

        @Override
        public OutputStream wrapOutput(OutputStream out) throws IOException {
            throw new UnsupportedOperationException("AUTO cannot compress.");
        }

        @Override
        public InputStream wrapInput(InputStream in) throws IOException {
            throw new UnsupportedOperationException("AUTO cannot decompress");
        }
    };

    private String compName;

    private CompressionMode() {
        this(null);
    }

    private CompressionMode(String name) {
        compName = name;
    }

    public String getCompressorName() {
        return compName;
    }

    /**
     * Get the effective compression mode.  For {@link #AUTO}, this auto-detects the appropriate
     * mode from the file name.
     *
     * @param filename The filename to compress.
     * @return The compression mode. Will never be {@link #AUTO}.
     */
    public CompressionMode getEffectiveCompressionMode(String filename) {
        return this;
    }

    /**
     * Wrap an output stream in this compression mode.
     * @param out The stream to wrap
     * @return A stream that will write its output, compressed as appropriate, to {@code out}.
     * @throws IOException If there is an error setting up the compressor.
     * @throws UnsupportedOperationException if the mode is {@link #AUTO} (use {@link #getEffectiveCompressionMode(String)} first).
     */
    public OutputStream wrapOutput(OutputStream out) throws IOException {
        if (compName == null) {
            return out;
        } else {
            try {
                return new CompressorStreamFactory().createCompressorOutputStream(compName, out);
            } catch (CompressorException e) {
                throw new IOException("Error setting up compressor", e);
            }
        }
    }

    /**
     * Wrap an input stream in a decompressor.
     * @param in The input stream.
     * @return An input stream that wraps {@code in} and decompresses as appropriate.
     * @throws IOException If there is an error setting up the decompressor.
     * @throws UnsupportedOperationException if the mode is {@link #AUTO} (use {@link #getEffectiveCompressionMode(String)} first).
     */
    public InputStream wrapInput(InputStream in) throws IOException {
        if (compName == null) {
            return in;
        } else {
            try {
                return new CompressorStreamFactory().createCompressorInputStream(compName, in);
            } catch (CompressorException e) {
                throw new IOException("Error setting up decompressor", e);
            }
        }
    }
}
