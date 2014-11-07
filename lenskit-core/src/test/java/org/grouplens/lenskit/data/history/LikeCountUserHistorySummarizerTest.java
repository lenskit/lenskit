package org.grouplens.lenskit.data.history;

import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class LikeCountUserHistorySummarizerTest {
    UserHistorySummarizer sum;

    @Before
    public void createSummarizer() {
        sum = new LikeCountUserHistorySummarizer();
    }

    @Test
    public void testSummarizeEmpty() {
        SparseVector vec = sum.summarize(History.forUser(42));
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testSummarizeNoLike() {
        SparseVector vec = sum.summarize(History.forUser(42, Ratings.make(42, 39, 2.5)));
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testOneLike() {
        SparseVector vec = sum.summarize(History.forUser(42, Events.like(42, 39)));
        assertThat(vec.size(), equalTo(1));
        assertThat(vec.get(39), equalTo(1.0));
    }

    @Test
    public void testTwoLikes() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.like(42, 39),
                                                         Events.like(42, 67)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(1.0));
        assertThat(vec.get(67), equalTo(1.0));
    }

    @Test
    public void testRepeatedLikes() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.like(42, 39),
                                                         Events.like(42, 67),
                                                         Events.like(42, 39)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(2.0));
        assertThat(vec.get(67), equalTo(1.0));
    }

    @Test
    public void testLikeBatch() {
        SparseVector vec = sum.summarize(History.forUser(42,
                                                         Events.like(42, 39),
                                                         Events.likeBatch(42, 67, 402),
                                                         Events.like(42, 39)));
        assertThat(vec.size(), equalTo(2));
        assertThat(vec.get(39), equalTo(2.0));
        assertThat(vec.get(67), equalTo(402.0));
    }
}
