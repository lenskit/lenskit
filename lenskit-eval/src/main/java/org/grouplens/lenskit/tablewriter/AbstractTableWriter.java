/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.tablewriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base implementation for table builders.  Implements everything in terms of
 * {@link #writeRow(String[])}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractTableWriter implements TableWriter {
    private final ReentrantLock lock = new ReentrantLock(true);
    protected final String[] columns;
    protected String[] values;

    protected AbstractTableWriter(String[] columns) {
        this.columns = columns;
    }

    /**
     * Query whether a row is active on the current thread.
     * @return <tt>true</tt> iff the current thread has a row active.
     */
    protected boolean isRowActive() {
        return lock.isHeldByCurrentThread();
    }

    @Override
    public void startRow() {
        if (isRowActive())
            throw new IllegalStateException("Row already in progress");
        lock.lock();
        try {
            if (values == null)
                values = new String[columns.length];
            else
                Arrays.fill(values, null);
        } catch (RuntimeException e) {
            lock.unlock();
            throw e;
        }
    }

    @Override
    public void finishRow() throws IOException {
        if (!isRowActive())
            throw new IllegalStateException("No row in progress");
        try {
            writeRow(values);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cancelRow() {
        if (!isRowActive())
            throw new IllegalStateException("No row in progress");
        values = null;
        lock.unlock();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    private void requireRow() {
        if (!isRowActive())
            throw new IllegalStateException("No row in progress");
    }

    @Override
    public void setValue(int col, long val) {
        requireRow();
        values[col] = Long.toString(val);
    }

    @Override
    public void setValue(int col, double val) {
        requireRow();
        values[col] = Double.toString(val);
    }

    @Override
    public void setValue(int col, String val) {
        requireRow();
        values[col] = val;
    }

    @Override
    public <V> void writeRow(Map<String, V> data) throws IOException {
        String[] vals = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            V v = data.get(columns[i]);
            if (v != null)
                vals[i] = v.toString();
        }
        writeRow(vals);
    }

    @Override
    public void writeRow(Object... columns) throws IOException {
        String[] cols = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            Object o = columns[i];
            cols[i] = o == null ? null : o.toString();
        }
        writeRow(cols);
    }
}
