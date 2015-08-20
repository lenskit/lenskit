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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;

import java.util.Map;

/**
 * Wrapper for long-to-double functions.
 */
class Long2DoubleFunctionWrapper implements Long2DoubleFunction {

    private final Map<Long, Double> map;
    private double defaultReturnValue = 0;

    public Long2DoubleFunctionWrapper(Map<Long, Double> map) {
        this.map = map;
    }

    @Override
    public double get(long l) {
        Double d = map.get(l);
        if (d == null) {
            return defaultReturnValue();
        } else {
            return d;
        }
    }

    @Override
    public double put(long key, double value) {
        Double rv = map.put(key, value);
        return rv != null ? rv : defaultReturnValue();
    }

    @Override
    public Double put(Long ok, Double ov) {
        return map.put(ok, ov);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public double remove(long l) {
        Double v = map.remove(l);
        if (v == null) {
            return defaultReturnValue();
        } else {
            return v;
        }
    }

    @Override
    public boolean containsKey(long l) {
        return map.containsKey(l);
    }

    @Override
    public void defaultReturnValue(double v) {
        defaultReturnValue = v;
    }

    @Override
    public double defaultReturnValue() {
        return defaultReturnValue;
    }

    @Override
    public Double get(Object o) {
        return map.get(o);
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Double remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void clear() {
        map.clear();
    }
}
