/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
/**
 *
 */
package org.grouplens.lenskit.knn.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.Comparator;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestTruncatingSimilarityMatrixBuilder {
    private static final double EPSILON = 1.0e-6;

    private TruncatingSimilarityMatrixAccumulator builder;

    @Before
    public void createBuilder() {
        LongArrayList items = new LongArrayList(10);
        for (long i = 0; i < 10; i++)
            items.add(i);
        builder = new TruncatingSimilarityMatrixAccumulator(5, items);
    }

    @Test
    public void testEmptyMatrix() {
        SimilarityMatrix matrix = builder.build();
        for (int i = 0; i < 10; i++) {
            assertTrue(matrix.getNeighbors(i).isEmpty());
        }
    }

    @Test
    public void testPopulateRow() {
        builder.put(0, 2, 0.2);
        builder.put(0, 3, 0.5);
        builder.put(0, 7, 0.4);
        SimilarityMatrix matrix = builder.build();
        ScoredLongList neighbors = matrix.getNeighbors(0);
        assertEquals(3, neighbors.size());
        assertEquals(2, neighbors.getLong(2));
        assertEquals(7, neighbors.getLong(1));
        assertEquals(3, neighbors.getLong(0));
        assertEquals(0.4, neighbors.getScore(1), EPSILON);
        for (int i = 1; i < 10; i++) {
            assertTrue(matrix.getNeighbors(i).isEmpty());
        }
    }

    @Test
    public void testPopulateRowSymmetric() {
        builder.putSymmetric(0, 2, 0.2);
        builder.putSymmetric(0, 3, 0.5);
        builder.putSymmetric(0, 7, 0.4);
        SimilarityMatrix matrix = builder.build();
        ScoredLongList neighbors = matrix.getNeighbors(0);
        assertEquals(3, neighbors.size());
        assertEquals(2, neighbors.getLong(2));
        assertEquals(7, neighbors.getLong(1));
        assertEquals(3, neighbors.getLong(0));
        assertEquals(0.4, neighbors.getScore(1), EPSILON);

        neighbors = matrix.getNeighbors(2);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.2, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(3);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.5, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(7);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.4, neighbors.getScore(0), EPSILON);
    }

    @Test
    public void testOverflowRow() {
        builder.put(0, 2, 0.2);
        builder.put(0, 3, 0.5);
        builder.put(0, 7, 0.4);
        builder.put(0, 5, 1.0);
        builder.put(0, 4, 0.7);
        builder.put(0, 8, 0.9);
        builder.put(0, 6, 0.1);
        SimilarityMatrix matrix = builder.build();
        ScoredLongList neighbors = matrix.getNeighbors(0);
        assertEquals(5, neighbors.size());
        final int[] expInd = {5, 8, 4, 3, 7};
        final double[] expScore = {1.0, 0.9, 0.7, 0.5, 0.4};
        for (int i = 0; i < 5; i++) {
            assertEquals(expInd[i], neighbors.getLong(i));
            assertEquals(expScore[i], neighbors.getScore(i), EPSILON);
        }
    }

    @Test
    public void testOverflowRowSymmetric() {
        builder.putSymmetric(0, 2, 0.2);
        builder.putSymmetric(0, 3, 0.5);
        builder.putSymmetric(0, 7, 0.4);
        builder.putSymmetric(0, 5, 1.0);
        builder.putSymmetric(0, 4, 0.7);
        builder.putSymmetric(0, 8, 0.9);
        builder.putSymmetric(0, 6, 0.1);
        SimilarityMatrix matrix = builder.build();
        ScoredLongList neighbors = matrix.getNeighbors(0);
        assertEquals(5, neighbors.size());
        final int[] expInd = {5, 8, 4, 3, 7};
        final double[] expScore = {1.0, 0.9, 0.7, 0.5, 0.4};
        for (int i = 0; i < 5; i++) {
            assertEquals(expInd[i], neighbors.getLong(i));
            assertEquals(expScore[i], neighbors.getScore(i), EPSILON);
        }

        assertTrue(Iterables.isEmpty(matrix.getNeighbors(1)));
        assertTrue(Iterables.isEmpty(matrix.getNeighbors(9)));

        neighbors = matrix.getNeighbors(2);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.2, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(3);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.5, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(7);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.4, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(6);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(0.1, neighbors.getScore(0), EPSILON);

        neighbors = matrix.getNeighbors(5);
        assertEquals(1, neighbors.size());
        assertEquals(0, neighbors.getLong(0));
        assertEquals(1.0, neighbors.getScore(0), EPSILON);
    }

    static class ScoreComparator implements Comparator<IndexedItemScore> {
        @Override
        public int compare(IndexedItemScore arg0, IndexedItemScore arg1) {
            return Doubles.compare(arg0.getScore(), arg1.getScore());
        }
    }
}
