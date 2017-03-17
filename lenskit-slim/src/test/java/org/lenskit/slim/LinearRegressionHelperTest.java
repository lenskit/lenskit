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

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.junit.Test;
import org.lenskit.util.math.Scalars;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

import static org.lenskit.slim.LinearRegressionHelper.transposeMap;


/**
 * Created by tmc on 2/19/17.
 */
public class LinearRegressionHelperTest {
    @Test
    public void testTransposeMap() {
        final Logger logger = LoggerFactory.getLogger(org.lenskit.slim.LinearRegressionHelperTest.class);
        Long2DoubleMap temp = new Long2DoubleOpenHashMap();
        temp.put(1, 2.0);
        temp.put(2, 3.0);
        temp.put(3, 4.0);
        Map<Long, Long2DoubleMap> mapT = Maps.newHashMap();
        mapT.put((long)1, temp);
        mapT.put((long)2, temp);
        Map<Long, Long2DoubleMap> map = transposeMap(mapT);
        logger.info("transpose matrix is {}, original matrix is {}", map, mapT);
        LongOpenHashBigSet itemSet = new LongOpenHashBigSet(map.keySet());
        logger.info("itemSet {}", itemSet);
    }

    @Test
    public void testHashMap() {
        final Logger logger = LoggerFactory.getLogger(org.lenskit.slim.LinearRegressionHelperTest.class);
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 2.0);
        map.put(2L, 2.0);
        for (double v : map.values()) {
            logger.info("map values set is {}", v);
        }
    }

    @Test
    public void testHashMapRemove() {
        final Logger logger = LoggerFactory.getLogger(org.lenskit.slim.LinearRegressionHelperTest.class);
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 2.0);
        map.put(2L, 3.0);
        map.put(3L, 4.0);
        Map<Long,Long2DoubleMap> matrix = Maps.newConcurrentMap();
        for (long i = 1; i < 4; i++) {
            matrix.put(i, map);
        }
        logger.info("matrix is {}", matrix);
        for (Map.Entry<Long, Long2DoubleMap> entry : matrix.entrySet()) {
            long itemKey = entry.getKey();
            Long2DoubleMap itemS = new Long2DoubleOpenHashMap(entry.getValue());
            itemS.remove(itemKey);
            matrix.put(itemKey, itemS);
        }
        logger.info("after remove matrix is {}", matrix);
    }

    @Test
    public void testComputeSimilarity() {
        final Logger logger = LoggerFactory.getLogger(LinearRegressionHelperTest.class);
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        map.put(1L, 0.0);
        map.put(2L, 1.0);
        map.put(3L, 0.0);
        Map<Long,Long2DoubleMap> matrix = Maps.newConcurrentMap();
        matrix.put(1L, map);
        map = new Long2DoubleOpenHashMap();
        map.put(1L, 0.0);
        map.remove(2L);
        map.put(3L, Math.sqrt(3.0));
        matrix.put(2L, map);
        map = new Long2DoubleOpenHashMap();
        map.put(1L, 0.0);
        map.put(2L, 1.0);
        map.put(3L, Math.sqrt(3.0));
        matrix.put(3L, map);
        logger.info("matrix is {}", matrix);

        Map<Long,Long2DoubleMap> itemSimilarities = Maps.newHashMap();
        Map<Long,Long2DoubleMap> innerProducts = Maps.newHashMap();

        // Ignore nonpositive similarities
        LongOpenHashBigSet itemIdSet = new LongOpenHashBigSet(matrix.keySet());
        Iterator<Map.Entry<Long, Long2DoubleMap>> iter = matrix.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Long, Long2DoubleMap> entry = iter.next();
            long itemId = entry.getKey();
            Long2DoubleMap itemIRatings = entry.getValue();
            itemIdSet.remove(itemId);
            Long2DoubleMap dotII = innerProducts.get(itemId);
            if (dotII == null) dotII = new Long2DoubleOpenHashMap();
            double dotProdII = Vectors.dotProduct(itemIRatings, itemIRatings);
            dotII.put(itemId, dotProdII);
            innerProducts.put(itemId, dotII);
            for (long itemJId : itemIdSet) {
                Long2DoubleMap itemJRatings = matrix.get(itemJId);
                double numerator = Vectors.dotProduct(itemIRatings, itemJRatings);
                double denominator = Vectors.euclideanNorm(itemIRatings) * Vectors.euclideanNorm(itemJRatings);
                double cosineSimilarity = 0.0;
                if (!Scalars.isZero(denominator)) cosineSimilarity = numerator/denominator;
                // storing similarities
                if (cosineSimilarity > 0.0) {
                    Long2DoubleMap simJI = itemSimilarities.get(itemJId);
                    Long2DoubleMap simIJ = itemSimilarities.get(itemId);
                    if (simJI == null) simJI = new Long2DoubleOpenHashMap();
                    if (simIJ == null) simIJ = new Long2DoubleOpenHashMap();
                    simJI.put(itemId, cosineSimilarity);
                    simIJ.put(itemJId, cosineSimilarity);
                    itemSimilarities.put(itemJId, simJI);
                    itemSimilarities.put(itemId, simIJ);
                    // storing interProducts used for SLIM learning
                    Long2DoubleMap dotJI = innerProducts.get(itemJId);
                    Long2DoubleMap dotIJ = innerProducts.get(itemId);
                    if (dotJI == null) dotJI = new Long2DoubleOpenHashMap();
                    if (dotIJ == null) dotIJ = new Long2DoubleOpenHashMap();
                    dotJI.put(itemId, numerator);
                    dotIJ.put(itemJId, numerator);
                    innerProducts.put(itemJId, dotJI);
                    innerProducts.put(itemId, dotIJ);
                }
            }
        }

        logger.info("similarities matrix is {}", itemSimilarities);
        logger.info("innerProducts matrix is {}", innerProducts);
    }

}
