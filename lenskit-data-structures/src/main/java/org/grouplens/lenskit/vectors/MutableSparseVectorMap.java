/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.vectors;

/**
 * Mutable extension of {@link SparseVectorMap}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class MutableSparseVectorMap extends SparseVectorMap {
    private final MutableSparseVector msv;

    MutableSparseVectorMap(MutableSparseVector vec) {
        super(vec);
        msv = vec;
    }

    @Override
    public Double put(long key, Double value) {
        if (msv.containsKey(key)) {
            return msv.set(key, value);
        } else if (msv.keyDomain().contains(key)) {
            msv.set(key, value);
            return defaultReturnValue();
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public Double remove(long key) {
        Double rv = defaultReturnValue();
        if (msv.containsKey(key)) {
            rv = msv.get(key);
            msv.unset(key);
        }
        return rv;
    }

    @Override
    public void clear() {
        msv.clear();
    }
}
