package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.util.test.MockItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FallbackItemScorerTest {
    ItemScorer primary;
    ItemScorer baseline;
    ItemScorer scorer;

    @Before
    public void setupScorer() {
        primary = MockItemScorer.newBuilder()
                                .addScore(42, 39, 3.5)
                                .build();
        baseline = MockItemScorer.newBuilder()
                                 .addScore(42, 39, 2.0)
                                 .addScore(42, 30, 4.0)
                                 .addScore(15, 30, 5.0)
                                 .build();
        scorer = new FallbackItemScorer(primary, baseline);
    }

    @Test
    public void testScoreItemPrimary() {
        // score known by the primary
        assertThat(scorer.score(42, 39), equalTo(3.5));
    }

    @Test
    public void testFallbackItem() {
        // score for item only known by secondary
        assertThat(scorer.score(42, 30), equalTo(4.0));
    }

    @Test
    public void testFallbackUser() {
        // score for user only known by secondary
        assertThat(scorer.score(15, 30), equalTo(5.0));
    }

    @Test
    public void testMultiple() {
        MutableSparseVector msv = MutableSparseVector.create(10, 30, 39);
        scorer.score(42, msv);
        assertThat(msv.size(), equalTo(2));
        assertThat(msv.get(39), equalTo(3.5));
        assertThat(msv.get(30), equalTo(4.0));
        assertThat(msv.unsetKeySet(), contains(10L));
    }
}
