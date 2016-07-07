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
package org.lenskit.util.table.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.LKFileUtils;

import com.google.common.io.Files;
import org.lenskit.util.table.TableLayout;

import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

/**
 * Implementation of {@link TableWriter} for CSV files.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CSVWriter extends AbstractTableWriter {
    private BufferedWriter writer;
    private TableLayout layout;

    /**
     * Construct a new CSV writer.
     *
     * @param w The underlying writer to output to.
     * @param l The table layout, or {@code null} if the table has no headers.
     * @throws IOException if there is an error writing the column headers.
     */
    public CSVWriter(@Nonnull Writer w, @Nullable TableLayout l) throws IOException {
        Preconditions.checkNotNull(w, "writer must not be null");
        layout = l;
        if (w instanceof BufferedWriter) {
            writer = (BufferedWriter) w;
        } else {
            writer = new BufferedWriter(w);
        }
        if (layout != null) {
            writeRow(layout.getColumns().toArray(new Object[l.getColumnCount()]));
        }
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    @Override
    public synchronized void writeRow(List<?> row) throws IOException {
        Preconditions.checkState(writer != null, "writer has been closed");
        if (layout != null) {
            checkRowWidth(row.size());
        }

        boolean first = true;
        for (Object val: row) {
            if (!first) {
                writer.write(',');
            }
            first = false;
            if (val != null) {
                writer.write(escapeCsv(val.toString()));
            }
        }
        writer.newLine();
        writer.flush();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    /**
     * Open a CSV writer to write to a file.
     *
     * @param file        The file to write to.
     * @param layout      The layout of the table.
     * @param compression What compression, if any, to use.
     * @return A CSV writer outputting to {@code file}.
     * @throws IOException if there is an error opening the file or writing the column header.
     */
    public static CSVWriter open(File file, TableLayout layout, CompressionMode compression) throws IOException {
        Files.createParentDirs(file);
        Writer writer = LKFileUtils.openOutput(file, Charset.defaultCharset(), compression);
        try {
            return new CSVWriter(writer, layout);
        } catch (Exception ex) {
            Closeables.close(writer, true);
            Throwables.propagateIfInstanceOf(ex, IOException.class);
            throw Throwables.propagate(ex);
        }
    }

    /**
     * Open a CSV writer to write to an auto-compressed file. The file will be compressed if its
     * name ends in ".gz".
     *
     * @param file   The file.
     * @param layout The table layout.
     * @return The CSV writer.
     * @throws IOException if there is an error opening the file or writing the column header.
     * @see #open(File, TableLayout, CompressionMode)
     */
    public static CSVWriter open(File file, @Nullable TableLayout layout) throws IOException {
        return open(file, layout, CompressionMode.AUTO);
    }
}
