package org.grouplens.lenskit.knn.user;

import java.util.Comparator;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Representation of a single neighboring user.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Neighbor {
    public final long userId;
    public final SparseVector ratings;
    public final double similarity;
    public Neighbor(long user, SparseVector rv, double sim) {
        userId = user;
        ratings = rv;
        similarity = sim;
    }
    
    /**
     * Comparator to order neighbors by similarity.
     */
    public static final Comparator<Neighbor> SIMILARITY_COMPARATOR = new Comparator<Neighbor>() {
        @Override
        public int compare(Neighbor n1, Neighbor n2) {
            return Double.compare(n1.similarity, n2.similarity);
        }
    };
}
