package org.grouplens.lenskit.util;

import org.grouplens.lenskit.collections.ScoredLongList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class TestTopNScoredItemAccumulator {
    ScoredItemAccumulator accum;

    @Before
    public void createAccumulator() {
        accum = new TopNScoredItemAccumulator(3);
    }

    @Test
    public void testEmpty() {
        ScoredLongList out = accum.finish();
        assertThat(out, Matchers.<Long>empty());
    }

    @Test
    public void testAccum() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        ScoredLongList out = accum.finish();
        assertThat(out, hasSize(3));
        assertThat(out.get(0), equalTo(2L));
        assertThat(out.getScore(0), equalTo(9.8));
        assertThat(out.get(1), equalTo(5L));
        assertThat(out.getScore(1), equalTo(4.2));
        assertThat(out.get(2), equalTo(3L));
        assertThat(out.getScore(2), equalTo(2.9));
    }

    @Test
    public void testAccumLimit() {
        accum.put(7, 1.0);
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        accum.put(8, 2.1);
        ScoredLongList out = accum.finish();
        assertThat(out, hasSize(3));
        assertThat(out.get(0), equalTo(2L));
        assertThat(out.getScore(0), equalTo(9.8));
        assertThat(out.get(1), equalTo(5L));
        assertThat(out.getScore(1), equalTo(4.2));
        assertThat(out.get(2), equalTo(3L));
        assertThat(out.getScore(2), equalTo(2.9));
    }
}
