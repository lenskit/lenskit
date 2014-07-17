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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.longs.LongLists;
import org.grouplens.lenskit.indexes.IdIndexMapping;
import org.grouplens.lenskit.indexes.MutableIdIndexMapping;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class VectorsTest {
    @Test
    public void testFromEmptyArray() {
        MutableSparseVector msv =
                Vectors.fromArray(IdIndexMapping.create(LongLists.EMPTY_LIST),
                                  new double[0]);
        assertThat(msv.size(), equalTo(0));
    }

    @Test
    public void testFromSingletonArray() {
        MutableSparseVector msv =
                Vectors.fromArray(IdIndexMapping.create(LongLists.singleton(10)),
                                  new double[]{Math.PI});
        assertThat(msv.size(), equalTo(1));
        assertThat(msv.get(10), equalTo(Math.PI));
    }

    @Test
    public void testFromBiggerArray() {
        MutableIdIndexMapping map = new MutableIdIndexMapping();
        map.internId(42);
        map.internId(39);
        MutableSparseVector msv =
                Vectors.fromArray(map, new double[]{Math.PI, Math.E});
        assertThat(msv.size(), equalTo(2));
        assertThat(msv.get(42), equalTo(Math.PI));
        assertThat(msv.get(39), equalTo(Math.E));
    }
}
