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

import javax.annotation.WillNotClose;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public final class TableWriters {
    private TableWriters() {
    }

    /**
     * Create a table writer that does nothing.
     * @return A table writer that discards all rows.
     */
    public static TableWriter noop(TableLayout layout) {
        return new DevNullTableWriter(layout);
    }

    /**
     * Create a table writer that writes data with common leading columns to an
     * underlying table writer.  The underlying writer will not be closed when the prefixed writer
     * is closed.
     *
     * @param base   The base table writer for output.
     * @param prefix The values of the leading columns in this table writer.
     * @return A table writer with
     *         {@code base.getColumnCount() - prefix.size()} columns. Each
     *         row is prefixed with the values in <var>prefix</var>.
     * @since 1.1
     */
    public static TableWriter prefixed(@WillNotClose TableWriter base, List<?> prefix) {
        return new PrefixedTableWriter(base, prefix);
    }

    /**
     * Create a table writer that writes data with common leading columns to an
     * underlying table writer.
     *
     * @param base   The base table writer for output.
     * @param prefix The values of the leading columns in this table writer.
     * @return A table writer with
     *         {@code base.getColumnCount() - prefix.length} columns. Each
     *         row is prefixed with the values in <var>prefix</var>.
     * @since 0.8
     */
    public static TableWriter prefixed(@WillNotClose TableWriter base, Object... prefix) {
        return prefixed(base, Arrays.asList(prefix));
    }
}
