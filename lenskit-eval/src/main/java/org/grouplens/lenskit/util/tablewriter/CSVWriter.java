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

import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of {@link TableWriter} for CSV files.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CSVWriter implements TableWriter {
    private Writer writer;
    private String[] columns;

    CSVWriter(Writer w, String[] cnames) throws IOException {
        columns = cnames;
        writer = w;
        writeRow(cnames);
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
        if (row.length > columns.length) {
            throw new RuntimeException("row too long");
        }

        for (int i = 0; i < columns.length; i++) {
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
    public int getColumnCount() {
        return columns.length;
    }

}
