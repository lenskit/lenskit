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

import java.io.IOException;
import java.util.List;

/**
 * Table writer that discards all data.
 */
public class DevNullTableWriter implements TableWriter {
    private final TableLayout layout;

    public DevNullTableWriter(TableLayout tl) {
        layout = tl;
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public void writeRow(Object... row) throws IOException {
        /* do nothing */
    }

    @Override
    public void writeRow(List<?> row) throws IOException {
        /* do nothing */
    }

    @Override
    public void flush() throws IOException {
        /* do nothing */
    }

    @Override
    public void close() throws IOException {
        /* do nothing */
    }
}
