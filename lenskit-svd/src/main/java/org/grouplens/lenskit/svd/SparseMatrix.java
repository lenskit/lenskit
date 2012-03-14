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
package org.grouplens.lenskit.svd;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMaps;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.Serializable;
import java.util.List;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SparseMatrix implements Serializable {
    private static final long serialVersionUID = -2396174756253967431L;
    private final List<Int2DoubleMap> data;

    public SparseMatrix() {
        data = new ObjectArrayList<Int2DoubleMap>();
    }

    public SparseMatrix (int nrows) {
        data = new ObjectArrayList<Int2DoubleMap>(nrows);
        for (int i = 0; i < nrows; i++) {
            Int2DoubleMap m = new Int2DoubleOpenHashMap();
            m.defaultReturnValue(Double.NaN);
            data.add(m);
        }
    }

    public void set(int row, int col, double value) {
        while (row >= data.size()) {
            Int2DoubleMap m = new Int2DoubleOpenHashMap();
            m.defaultReturnValue(Double.NaN);
            data.add(m);
        }
        data.get(row).put(col, value);
    }

    public double get(int row, int col) {
        if (row >= data.size()) {
            return Double.NaN;
        } else {
            return data.get(row).get(col);
        }
    }

    public Int2DoubleMap row(int row) {
        if (row >= data.size()) {
            return Int2DoubleMaps.EMPTY_MAP;
        } else {
            return Int2DoubleMaps.unmodifiable(data.get(row));
        }
    }
}
