/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
