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

import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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
     * @param l The table layout.
     * @throws IOException if there is an error writing the column headers.
     */
    public CSVWriter(Writer w, TableLayout l) throws IOException {
        layout = l;
        writer = w;
        writeRow(layout.getColumnHeaders().toArray(new String[l.getColumnCount()]));
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
        if (row.length > layout.getColumnCount()) {
            throw new IllegalArgumentException("row too long");
        }

        final int n = layout.getColumnCount();
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
     * Open a CSV writer on a file.
     * @param file The file to write to.
     * @param layout The layout of the table.
     * @return A CSV writer outputting to {@code file}.
     * @throws IOException if there is an error opening the file or writing the column header.
     */
    public static CSVWriter open(File file, TableLayout layout) throws IOException {
        Files.createParentDirs(file);
        return new CSVWriter(new FileWriter(file), layout);
    }
}
