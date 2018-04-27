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

import org.junit.Test;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultMapTest {
    @Test
    public void testEmptyMap() {
        ResultMap r = Results.newResultMap();
        assertThat(r.isEmpty(), equalTo(true));
        assertThat(r.size(), equalTo(0));
        assertThat(r.scoreMap().size(), equalTo(0));
        assertThat(r.getScore(42), notANumber());
    }

    @Test
    public void testSingletonMap() {
        ResultMap r = Results.<Result>newResultMap(Results.create(42L, 3.5));
        assertThat(r.size(), equalTo(1));
        assertThat(r, containsInAnyOrder((Result) Results.create(42L, 3.5)));
        assertThat(r.get(42L), equalTo((Result) Results.create(42L, 3.5)));
        assertThat(r.getScore(42), equalTo(3.5));
    }

    @Test
    public void testMultiMap() {
        ResultMap r = Results.<Result>newResultMap(Results.create(42L, 3.5),
                                                   Results.create(37L, 4.2));
        assertThat(r.size(), equalTo(2));
        assertThat(r.keySet(), containsInAnyOrder(42L, 37L));
        assertThat(r, containsInAnyOrder((Result) Results.create(42L, 3.5),
                                         (Result) Results.create(37L, 4.2)));
        assertThat(r.getScore(42), equalTo(3.5));
        assertThat(r.getScore(37), equalTo(4.2));
        assertThat(r.getScore(28), notANumber());
    }
}
