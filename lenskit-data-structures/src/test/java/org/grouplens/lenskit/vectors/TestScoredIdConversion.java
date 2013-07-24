package org.grouplens.lenskit.vectors;

import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestScoredIdConversion {
    ScoredIdListBuilder builder;

    @Before
    public void createBuilder() {
        builder = ScoredIds.newListBuilder();
    }

    @Test
    public void testEmpty() {
        MutableSparseVector vec = Vectors.fromScoredIds(Collections.<ScoredId>emptyList());
        assertThat(vec.size(), equalTo(0));
    }

    @Test
    public void testSingleton() {
        builder.add(1, 3.5);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(1));
        assertThat(vec.get(1), equalTo(3.5));
    }

    @Test
    public void testSome() {
        builder.add(1, 3.5);
        builder.add(3, 5.2);
        builder.add(-1, 0.2);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(3));
        assertThat(vec.get(1), equalTo(3.5));
        assertThat(vec.get(3), equalTo(5.2));
        assertThat(vec.get(-1), equalTo(0.2));
    }

    @Test
    public void testDuplicate() {
        builder.add(1, 3.5);
        builder.add(3, 5.2);
        builder.add(-1, 0.2);
        builder.add(3, 0.8);
        MutableSparseVector vec = Vectors.fromScoredIds(builder.finish());
        assertThat(vec.size(), equalTo(3));
        assertThat(vec.get(1), equalTo(3.5));
        assertThat(vec.get(3), equalTo(5.2));
        assertThat(vec.get(-1), equalTo(0.2));
    }
}
