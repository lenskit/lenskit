package org.lenskit.results;

import net.java.quickcheck.collection.Pair;
import org.junit.Test;
import org.lenskit.api.Result;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.somePairs;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultTest {
    @Test
    public void testGetters() {
        for (Pair<Long,Double> pair: somePairs(longs(), doubles())) {
            Result r = new BasicResult(pair.getFirst(), pair.getSecond());
            assertThat(r.getId(), equalTo(pair.getFirst()));
            assertThat(r.getScore(), equalTo(pair.getSecond()));
            assertThat(r.hasScore(), equalTo(true));
        }
    }

    @Test
    public void testHasScore() {
        Result r = new BasicResult(42, Double.NaN);
        assertThat(r.hasScore(), equalTo(false));
    }

    @Test
    public void testEquality() {
        BasicResult result = new BasicResult(42, Math.PI);
        BasicResult equal = new BasicResult(42, Math.PI);
        BasicResult sameId = new BasicResult(42, Math.E);
        BasicResult sameScore = new BasicResult(37, Math.PI);
        BasicResult diff = new BasicResult(37, Math.E);

        assertThat(result.equals(null), equalTo(false));
        assertThat(result.equals(result), equalTo(true));
        assertThat(result.equals(equal), equalTo(true));
        assertThat(result.equals(sameId), equalTo(false));
        assertThat(result.equals(sameScore), equalTo(false));
        assertThat(result.equals(diff), equalTo(false));
    }
}
