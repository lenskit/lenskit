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

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.compressors.xz.XZUtils;

import java.io.File;
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

    /**
     * Auto-detect a compression mode from a file name.
     * @param name The file name.
     * @return The compression mode.  Will never be {@link #AUTO}.
     */
    public static CompressionMode autodetect(String name) {
        return AUTO.getEffectiveCompressionMode(name);
    }

    /**
     * Auto-detect a compression mode from a file's name.
     * @param file The file.
     * @return The compression mode.  Will never be {@link #AUTO}.
     */
    public static CompressionMode autodetect(File file) {
        return AUTO.getEffectiveCompressionMode(file.getName()
        );
    }
}
