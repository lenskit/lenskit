package org.grouplens.lenskit.scored;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ScoredIdsTest {
    @Test
    public void testIdFunction() {
        assertThat(ScoredIds.idFunction().apply(null),
                   nullValue());
        ScoredId id = ScoredIds.create(42, 39);
        assertThat(ScoredIds.idFunction().apply(id),
                   equalTo(42L));
    }
}
