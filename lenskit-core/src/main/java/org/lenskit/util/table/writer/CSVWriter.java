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
package org.lenskit.util.table.writer;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.io.LKFileUtils;
import org.lenskit.util.table.TableLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

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
    public CSVWriter(@WillCloseWhenClosed @Nonnull Writer w, @Nullable TableLayout l) throws IOException {
        Preconditions.checkNotNull(w, "writer");

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
    public synchronized void flush() throws IOException {
        writer.flush();
    }

    @Override
    public synchronized void close() throws IOException {
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
            if (val instanceof Number) {
                writer.write(val.toString());
            } else if (val != null) {
                writer.write(escapeCsv(val.toString()));
            }
        }
        writer.newLine();
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
        } catch (Throwable th) {
            try {
                writer.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            Throwables.propagateIfInstanceOf(th, IOException.class);
            throw Throwables.propagate(th);
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
