package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.math.Scalars;
import org.lenskit.util.math.Vectors;

import java.util.Iterator;
import java.util.Map;
import static java.lang.Math.abs;


/**Several simple vector algebra used by SLIM learning process
 * Created by tmc on 2/10/17.
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
        if (z < 0 && gamma < abs(z)) return z + gamma; // comment this line to get non-zero weights during learning process
        return 0.0;
    }

    /**
     * transpose of matrix
     * @param matrix
     * @return
     */
    public static Map<Long, Long2DoubleMap> transposeMap(Map<Long, Long2DoubleMap> matrix) {
        Map<Long, Long2DoubleMap> mapT = Maps.newHashMap();
        //Iterator<Map.Entry<Long, Long2DoubleMap>> iter = matrix.entrySet().iterator();
        for (Map.Entry<Long, Long2DoubleMap> rowEntry : matrix.entrySet()) {
            long rowNum = rowEntry.getKey();
            Long2DoubleMap rowValue = rowEntry.getValue();
            for (Map.Entry<Long, Double> entry : rowValue.entrySet()) {
                long colNum = entry.getKey();
                double Value = entry.getValue();
//                Long2DoubleMap colEntry = new Long2DoubleOpenHashMap();
//                colEntry.put(rowNum, Value);
//                mapT.putIfAbsent(colNum, colEntry);
//                mapT.get(colNum).put(rowNum, Value);
                Long2DoubleMap colEntry = mapT.get(colNum);
                if (colEntry == null) colEntry = new Long2DoubleOpenHashMap();
                colEntry.put(rowNum, Value);
                mapT.put(colNum, colEntry);
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
