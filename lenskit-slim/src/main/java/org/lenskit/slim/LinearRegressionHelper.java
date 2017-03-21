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
import org.lenskit.util.math.Scalars;
import java.util.Map;
import static java.lang.Math.abs;


/**
 * Several simple vector algebra used by SLIM learning process
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class LinearRegressionHelper {
    /**
     * soft-Thresholding for coordinate descent
     * @param z
     * @param gamma
     * @return
     */
    public static double softThresholding(double z, double gamma) {
        if (z > 0 && gamma < abs(z)) return z - gamma;
        if (z < 0 && gamma < abs(z)) return z + gamma; // comment this line to get non-negative weights during learning process
        return 0.0;
    }

    /**
     * Transpose of matrix
     * Convert a row view of rating matrix to a column view and vice versa
     * After conversion, the key of empty row/column in the input matrix will be eliminated in the transpose matrix.
     * So calling transposeMap(transposeMap(X)).keySet() may not equal to X.keySet() unless X has no mapping of key to empty Long2Double map.
     *
     * @param matrix
     * @return
     */
    public static Long2ObjectMap<Long2DoubleMap> transposeMap(Long2ObjectMap<Long2DoubleMap> matrix) {
        Long2ObjectMap<Long2DoubleMap> mapT = new Long2ObjectOpenHashMap<>();

        for (Map.Entry<Long, Long2DoubleMap> rowEntry : matrix.entrySet()) {
            long rowNum = rowEntry.getKey();
            Long2DoubleMap rowValue = rowEntry.getValue();
            for (Map.Entry<Long, Double> entry : rowValue.entrySet()) {
                long colNum = entry.getKey();
                double Value = entry.getValue();
                Long2DoubleMap rowOfMapT = mapT.get(colNum);
                if (rowOfMapT == null) rowOfMapT = new Long2DoubleOpenHashMap();
                rowOfMapT.put(rowNum, Value);
                mapT.put(colNum, rowOfMapT);
            }
        }
        return mapT;
    }

    /**
     * add two vectors
     * @param a
     * @param b
     * @return
     */
    public static Long2DoubleMap addVectors(Long2DoubleMap a, Long2DoubleMap b) {
        //Long2DoubleMap sumOfTwoVectors = Vectors.combine(a, b, 1.0, 0.0);
        Long2DoubleMap sumOfTwoVectors = new Long2DoubleOpenHashMap(a);
        for (Map.Entry<Long, Double> e : b.entrySet()) {
            long key = e.getKey();
            double value = e.getValue();
            double sum = sumOfTwoVectors.get(key) + value;
            sumOfTwoVectors.put(key, sum);
        }
        return sumOfTwoVectors;

    }

    /**
     * element-wise multiplication of two vectors.
     * @param a
     * @param b
     * @return
     */
    public static Long2DoubleMap multiply(Long2DoubleMap a, Long2DoubleMap b) {
        Long2DoubleMap productOfTwoVectors = new Long2DoubleOpenHashMap();
        LongOpenHashBigSet aSet = new LongOpenHashBigSet(a.keySet());
        LongOpenHashBigSet bSet = new LongOpenHashBigSet(b.keySet());
        aSet.retainAll(bSet); // intersection of two key sets
        //productOfTwoVectors.defaultReturnValue(0.0);
        //b.forEach((k, v) -> productOfTwoVectors.merge(k, v, (oldVal, newVal) -> oldVal * newVal));
        for (long key : aSet) {
            //double prod = a.getOrDefault(key, 0.0)*b.getOrDefault(key, 0.0);
            double prod = a.get(key)*b.get(key);
            productOfTwoVectors.put(key, prod);
        }
        return productOfTwoVectors;
    }

//    /**
//     * multiply vector by a real value
//     * @param a
//     * @param c
//     * @return
//     */
//    public static Long2DoubleMap multiply(Long2DoubleMap a, double c) {
//        Long2DoubleMap scalingVector = new Long2DoubleOpenHashMap();
//        a.forEach((k, v) -> scalingVector.put((long) k, (v*c)));
//        return scalingVector;
//    }

    /**
     * return a new map with all values in input map but not equal to the given value
     * @param a input map
     * @param value a value needs to be filtered out
     * @return
     */
    public static Long2DoubleMap filterValues(Long2DoubleMap a, double value) {
        Long2DoubleMap result = new Long2DoubleOpenHashMap();
        for (Map.Entry<Long, Double> e : a.entrySet()) {
            long key = e.getKey();
            double v = e.getValue();
            if (!Scalars.isZero(abs(v - value))) {
                result.put(key, v);
            }
        }
        return result;
    }

}
