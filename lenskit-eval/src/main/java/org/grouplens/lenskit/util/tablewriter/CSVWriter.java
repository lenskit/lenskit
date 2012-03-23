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
package org.grouplens.lenskit.util.tablewriter;

import com.google.common.io.Files;
import org.grouplens.lenskit.util.LKFileUtils;
import org.picocontainer.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of {@link TableWriter} for CSV files.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CSVWriter implements TableWriter {
    private Writer writer;
    private TableLayout layout;

    /**
     * Construct a new CSV writer.
     * @param w The underlying writer to output to.
     * @param l The table layout, or {@code null} if the table has no headers.
     * @throws IOException if there is an error writing the column headers.
     */
    public CSVWriter(@Nonnull Writer w, @Nullable TableLayout l) throws IOException {
        layout = l;
        writer = w;
        if (layout != null) {
            writeRow(layout.getColumnHeaders().toArray(new String[l.getColumnCount()]));
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
        writer = null;
    }

    String quote(String e) {
        if (e == null)
            return "";

        if (e.matches("[\r\n,\"]")) {
            return "\"" + e.replaceAll("\"", "\"\"") + "\"";
        } else {
            return e;
        }
    }

    @Override
    public synchronized void writeRow(String[] row) throws IOException {
        if (layout != null && row.length > layout.getColumnCount()) {
            throw new IllegalArgumentException("row too long");
        }

        final int n = layout == null ? row.length : layout.getColumnCount();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                writer.write(',');
            }
            if (i < row.length) {
                writer.write(quote(row[i]));
            }
        }
        writer.write('\n');
        writer.flush();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    /**
     * Open a CSV writer to write to a file.
     * @param file The file to write to.
     * @param layout The layout of the table.
     * @param compressed {@code true} to write the file with GZip compression.
     * @return A CSV writer outputting to {@code file}.
     * @throws IOException if there is an error opening the file or writing the column header.
     */
    public static CSVWriter open(File file, TableLayout layout, boolean compressed) throws IOException {
        Files.createParentDirs(file);
        OutputStream out = new FileOutputStream(file);
        try {
            if (compressed) {
                out = new GZIPOutputStream(out);
            }
            Writer writer = new OutputStreamWriter(out);
            return new CSVWriter(writer, layout);
        } catch (RuntimeException e) {
            LKFileUtils.close(out);
            throw e;
        } catch (IOException e) {
            LKFileUtils.close(out);
            throw e;
        }
    }

    /**
     * Open a CSV writer to write to an uncompressed file.
     * @param file The file.
     * @param layout The table layout.
     * @return The CSV writer.
     * @throws IOException if there is an error opening the file or writing the column header.
     * @see #open(File,TableLayout,boolean)
     */
    public static CSVWriter open(File file, @Nullable TableLayout layout) throws IOException {
        return open(file, layout, false);
    }
}
