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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * @author Michael Ekstrand
 */
public class VectorEntry implements Long2DoubleMap.Entry {
    private long key;
    private double value;

    public VectorEntry(long k, double v) {
        key = k;
        value = v;
    }

    public Long getKey() {
        return key;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public Double setValue(Double value) {
        throw new UnsupportedOperationException();
    }

    public long getLongKey() {
        return key;
    }

    @Override
    public double setValue(double v) {
        throw new UnsupportedOperationException();
    }

    public double getDoubleValue() {
        return value;
    }

    void set(long k, double v) {
        key = k;
        value = v;
    }
}
