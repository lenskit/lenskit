/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
