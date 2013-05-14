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

import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Re-run sparse vector tests with a longer version of the vector.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestSparseVectorSized extends TestMutableSparseVector {
    /**
     * Construct a simple rating vector with three ratings.
     *
     * @return A rating vector mapping {3, 7, 8} to {1.5, 3.5, 2}.
     */
    @Override
    protected MutableSparseVector simpleVector() {
        long[] keys = {3, 7, 8, 2};
        double[] values = {1.5, 3.5, 2, 5};
        return new MutableSparseVector(keys, values, 3);
    }
}
