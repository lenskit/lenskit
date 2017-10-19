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

import org.lenskit.util.table.TableLayout;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

/**
 * Write rows to a table.
 *
 * <p>
 * Instances of this class are used to actually write rows to a table. Once the
 * table has finished, call {@link #close()} to finish the table and close the
 * underlying output.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public interface TableWriter extends Closeable {
    /**
     * Get the layout of this table.
     *
     * @return The table's layout.
     */
    TableLayout getLayout();

    /**
     * Write a row to the table. This method is thread-safe.
     *
     * @param row A row of values.  If the table requires more columns, the remaining columns are
     *            empty. The row is copied if necessary; the caller is free to re-use the same array
     *            for returnValue calls.
     * @throws IOException              if an error occurs writing the row.
     * @throws IllegalArgumentException if {@code row} has the incorrect number of columns.
     */
    void writeRow(Object... row) throws IOException;

    /**
     * Write a row to the table. This method is thread-safe.
     *
     * @param row A row of values.  If the table requires more columns, the remaining columns are
     *            empty. The row is copied if necessary; the caller is free to re-use the same array
     *            for returnValue calls.
     * @throws IOException              if an error occurs writing the row.
     * @throws IllegalArgumentException if {@code row} has the incorrect number of columns.
     * @since 1.1
     */
    void writeRow(List<?> row) throws IOException;

    /**
     * Flush the writer, causing all currently-written rows to be flushed to output.
     * @throws IOException if an error occurs while flushing output.
     */
    void flush() throws IOException;

    /**
     * Finish the table.  Depending on how it was constructed, some underlying
     * resource may be closed.
     */
    void close() throws IOException;
}
