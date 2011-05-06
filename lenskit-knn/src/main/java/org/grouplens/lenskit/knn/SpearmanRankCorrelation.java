package org.grouplens.lenskit.knn;

import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.LongArrays;

import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

import com.google.common.primitives.Doubles;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SpearmanRankCorrelation implements
        OptimizableVectorSimilarity<SparseVector> {
    private PearsonCorrelation pearson = new PearsonCorrelation();
    
    public SpearmanRankCorrelation(int thresh) {
        if (thresh > 0)
            pearson = new SignificanceWeightedPearsonCorrelation(thresh);
        else
            pearson = new PearsonCorrelation();
    }
    
    public SpearmanRankCorrelation() {
        this(0);
    }
    
    static SparseVector rank(final SparseVector vec) {
        long[] ids = vec.keySet().toLongArray();
        // sort ID set by value (decreasing)
        LongArrays.quickSort(ids, new AbstractLongComparator() {
            @Override
            public int compare(long k1, long k2) {
                return Doubles.compare(vec.get(k2), vec.get(k1));
            }
        });
        
        final int n = ids.length;
        final double[] values = new double[n];
        MutableSparseVector rank = vec.mutableCopy();
        // assign ranks to each item
        for (int i = 0; i < n; i++) {
            rank.set(ids[i], i+1);
            values[i] = vec.get(ids[i]);
        }
        
        // average ranks for items with same values
        int i = 0;
        while (i < n) {
            int j;
            for (j = i+1; j < n; j++) {
                if (values[j] != values[i])
                    break;
            }
            if (j - i > 1) {
                double r2 = (rank.get(ids[i]) + rank.get(ids[j-1])) / (j - i);
                for (int k = i; k < j; k++)
                    rank.set(ids[k], r2);
            }
            i = j;
        }
        
        // Make a sparse vector out of it
        return rank;
    }

    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        return pearson.similarity(rank(vec1), rank(vec2));
    }
}
