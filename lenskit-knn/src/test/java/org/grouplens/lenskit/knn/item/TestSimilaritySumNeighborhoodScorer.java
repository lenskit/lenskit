package org.grouplens.lenskit.knn.item;

import static org.junit.Assert.assertThat;

import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class TestSimilaritySumNeighborhoodScorer {
    SimilaritySumNeighborhoodScorer scorer;
    
    @Before
    public void createScorer() {
        scorer = new SimilaritySumNeighborhoodScorer();
    }
    
    public static Matcher<Double> closeTo(double x) {
        return Matchers.closeTo(x, 1.0e-5);
    }

    @Test
    public void testEmpty() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        SparseVector scores = new MutableSparseVector();
        assertThat(scorer.score(nbrs, scores), closeTo(0));
    }
    
    @Test
    public void testEmptyNbrs() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        SparseVector scores = ImmutableSparseVector.wrap(new long[]{5}, new double[]{3.7});
        assertThat(scorer.score(nbrs, scores), closeTo(0));
    }
    
    @Test
    public void testOneNbr() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        nbrs.add(5, 1.0);
        SparseVector scores = ImmutableSparseVector.wrap(new long[]{5}, new double[]{3.7});
        assertThat(scorer.score(nbrs, scores), closeTo(1.0));
    }
    
    @Test
    public void testMultipleNeighbors() {
        ScoredLongList nbrs = new ScoredLongArrayList();
        nbrs.add(5, 1.0);
        nbrs.add(7, 0.92);
        nbrs.add(2, 0.5);
        long[] keys = {2, 3, 5, 7};
        double[] ratings = {3.7, 4.2, 1.2, 7.8};
        SparseVector scores = ImmutableSparseVector.wrap(keys, ratings);
        assertThat(scorer.score(nbrs, scores), closeTo(2.42));
    }
}
