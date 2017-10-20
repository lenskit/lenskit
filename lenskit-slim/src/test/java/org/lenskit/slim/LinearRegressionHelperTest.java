/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;
import org.lenskit.util.math.Vectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.CoreMatchers.is;


/**
 * Helper Class Test
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LinearRegressionHelperTest {

    @Test
    public void testAddVectors() throws Exception {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 2.0);
        map.put(2L, 3.0);
        map.put(3L, 4.0);
        map.put(5L, 3.0);
        Long2DoubleMap map1 = new Long2DoubleOpenHashMap();
        map1.put(2L, 3.0);
        map1.put(7L, 2.0);
        map1.put(5L, 4.0);
        Long2DoubleMap sum = Vectors.add(map, map1);
        Long2DoubleMap sumExpected = new Long2DoubleOpenHashMap();
        sumExpected.put(1L, 2.0);
        sumExpected.put(2L, 6.0);
        sumExpected.put(3L, 4.0);
        sumExpected.put(5L, 7.0);
        sumExpected.put(7L, 2.0);
        assertThat(sum.keySet(), containsInAnyOrder(1L,2L,3L,5L,7L));
        assertThat(sum.values(), containsInAnyOrder(2.0, 3+3.0, 4.0, 3+4.0, 2.0));
        assertThat(sum, is(sumExpected));
    }

    @Test
    public void testMultiply() throws Exception {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 2.0);
        map.put(2L, 3.0);
        map.put(3L, 4.0);
        map.put(5L, 3.0);
        Long2DoubleMap map1 = new Long2DoubleOpenHashMap();
        map1.put(2L, 3.0);
        map1.put(7L, 2.0);
        map1.put(5L, 4.0);
        Long2DoubleMap product = Vectors.ebeMultiply(map, map1);
        assertThat(product.keySet(), containsInAnyOrder(2L, 5L));
        assertThat(product.values(), containsInAnyOrder(3.0*3.0,3*4.0));

    }

    @Test
    public void testFilterValues() {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 2.0);
        map.put(2L, 3.0);
        map.put(3L, 4.0);
        map.put(5L, 3.0);
        Long2DoubleMap mapFiltered = Vectors.filterValues(map, 3.0, Double.MIN_VALUE);
        assertThat(mapFiltered.keySet(), containsInAnyOrder(1L, 3L));
        assertThat(mapFiltered.values(), containsInAnyOrder(2.0, 4.0));
    }

    @Test
    public void testTransposeMap() {
//        final Logger logger = LoggerFactory.getLogger(org.lenskit.slim.LinearRegressionHelperTest.class);
        Long2DoubleMap temp = new Long2DoubleOpenHashMap();
        temp.put(1L, 2.0);
        temp.put(2L, 3.0);
        temp.put(3L, 4.0);
        Long2ObjectMap<Long2DoubleMap> mapT = new Long2ObjectOpenHashMap<>();
        mapT.put(1L, temp);
        mapT.put(2L, temp);
        Long2ObjectMap<Long2DoubleMap> map = Vectors.transposeMap(mapT);
        //logger.info("transpose matrix is {}, original matrix is {}", map, mapT);

        assertThat(mapT.keySet().size(), equalTo(2));
        assertThat(map.keySet().size(), equalTo(3));

        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(map.keySet());

        assertThat(itemSet, containsInAnyOrder(1L, 3L, 2L));
        assertThat(mapT.keySet(), containsInAnyOrder(1L, 2L));

        Long2DoubleMap col1 = new Long2DoubleOpenHashMap();
        col1.put(1L, 2.0);
        col1.put(2L, 2.0);
        Long2DoubleMap col2 = new Long2DoubleOpenHashMap();
        col2.put(1L, 3.0);
        col2.put(2L, 3.0);
        Long2DoubleMap col3 = new Long2DoubleOpenHashMap();
        col3.put(1L, 4.0);
        col3.put(2L, 4.0);

        assertThat(map.values(), containsInAnyOrder(col1, col2, col3));
        //logger.info("itemSet {}", itemSet);

        Long2ObjectMap<Long2DoubleMap> mapExpected = new Long2ObjectOpenHashMap<>();
        mapExpected.put(1L, col1);
        mapExpected.put(2L, col2);
        mapExpected.put(3L, col3);
        assertThat(map, is(mapExpected));

    }


}
