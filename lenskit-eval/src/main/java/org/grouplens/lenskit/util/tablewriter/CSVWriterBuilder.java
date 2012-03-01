/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.util.tablewriter;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

/**
 * Write tables as CSV files.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CSVWriterBuilder implements TableWriterBuilder {
    private OutputSupplier<Writer> outputSupplier;
    private Charset encoding;
    private boolean compress;
    private boolean createDir = true;

    private String[] columns;

    public CSVWriterBuilder() {
        outputSupplier = new OutputSupplier<Writer>() {
            @Override
            public Writer getOutput() {
                throw new IllegalStateException("output not configured");
            }
        };
    }

    /**
     * Construct a CSV writer with a specific supplier of output streams.
     * @param output A supplier which will open a new output stream for the writer.
     */
    public CSVWriterBuilder(OutputSupplier<Writer> output) {
        outputSupplier = output;
    }

    /**
     * Construct a CSV writer that will output to the specified file.
     * @param outFile The output file.
     */
    public CSVWriterBuilder(File outFile) {
        setFile(outFile);
    }

    /**
     * Configure the output file for the resulting CSV writers. This overrides any
     * supplier set with {@link #setOutputSupplier(OutputSupplier)}.
     * @param outFile The file to write data to.
     */
    public void setFile(File outFile) {
        outputSupplier = new FileOutput(outFile);
    }

    /**
     * Get the file this builder will write to.  If an ouptut supplier is configured
     * directly, rather than via {@link #setFile(File)}, this will return {@code null}.
     * @return The name of the file.
     */
    @Nullable
    public File getFile() {
        if (outputSupplier instanceof FileOutput) {
            FileOutput out = (FileOutput) outputSupplier;
            return out.getFile();
        } else {
            return null;
        }
    }

    /**
     * Set the encoding used to write to a file. Ignored if the writer is configured
     * with {@link #setOutputSupplier(OutputSupplier)}.
     * @param encName The name of the encoding to use.
     * @see #setEncoding(Charset)
     */
    public void setEncoding(String encName) {
        setEncoding(Charset.forName(encName));
    }

    /**
     * Set the encoding used to write to a file. Ignored if the writer is configured
     * with {@link #setOutputSupplier(OutputSupplier)}.
     * @param enc The encoding to use.
     */
    public void setEncoding(Charset enc) {
        encoding = enc;
    }

    /**
     * Get the encoding to use.
     * @return The encoding the resulting table writer will use.
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * Specify whether the table will be compressed. Tables are compressed using
     * GZip compression. The default is to write tables uncompressed.
     * @param comp {@code true} to compress the output file, {@code false} otherwise.
     */
    public void setCompressed(boolean comp) {
        compress = comp;
    }

    /**
     * Query whether tables written by this builder will be compressed.
     * @return {@code true} iff tables will be compressed.
     */
    public boolean isCompressed() {
        return compress;
    }

    /**
     * Set a supplier to open output streams for writing tables. This overrides
     * any file set with {@link #setFile(File)}.
     * @param out A supplier of writers to write tables.
     */
    public void setOutputSupplier(OutputSupplier<Writer> out) {
        outputSupplier = out;
    }

    /**
     * If true, create any missing parent directories of the file before opening
     * it.  The default is {@code true}.
     * @param create {@code true} to create parent directories as necessary when
     *                           opening the file.
     * @see #setFile(File)
     */
    public void setCreateDirectory(boolean create) {
        createDir = create;
    }

    /**
     * Query whether this builder will create directories as necessary to open the
     * output file.
     * @return {@code true} if the writer will create directories when opened.
     */
    public boolean createsDirectory() {
        return createDir;
    }

    @Override
    public void setColumns(String[] names) {
        columns = names;
    }

    @Override
    public TableWriter open() throws IOException {
        Writer output = outputSupplier.getOutput();
        try {
            return new CSVWriter(output, columns);
        } catch (RuntimeException e) {
            output.close();
            throw e;
        }
    }

    class FileOutput implements OutputSupplier<Writer> {
        @Nonnull private File file;
        public FileOutput(@Nonnull File f) {
            file = f;
        }

        @Nonnull
        public File getFile() {
            return file;
        }

        @Override
        public Writer getOutput() throws IOException {
            if (createDir) {
                Files.createParentDirs(file);
            }
            OutputStream outs = new FileOutputStream(file);
            if (compress) {
                try {
                    outs = new GZIPOutputStream(outs);
                } catch (IOException x) {
                    Closeables.closeQuietly(outs);
                    throw x;
                } catch (RuntimeException x) {
                    Closeables.closeQuietly(outs);
                    throw x;
                }
            }
            Charset cs = encoding;
            if (cs == null) cs = Charset.defaultCharset();
            return new OutputStreamWriter(outs, cs);
        }
    }
}
