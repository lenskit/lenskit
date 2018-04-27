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
import org.lenskit.api.ResultList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultListTest {
    @Test
    public void testEmptyList() {
        ResultList r = Results.newResultList();
        assertThat(r.isEmpty(), equalTo(true));
        assertThat(r.size(), equalTo(0));
        assertThat(r.idList(), hasSize(0));
    }

    @Test
    public void testSingletonList() {
        ResultList r = Results.<Result>newResultList(Results.create(42L, 3.5));
        assertThat(r, hasSize(1));
        assertThat(r, contains((Result) Results.create(42L, 3.5)));
        assertThat(r.idList(), hasSize(1));
        assertThat(r.idList(), contains(42L));
    }

    @Test
    public void testMultiList() {
        ResultList r = Results.<Result>newResultList(Results.create(42L, 3.5),
                                                             Results.create(37L, 4.2));
        assertThat(r, hasSize(2));
        assertThat(r, contains((Result) Results.create(42L, 3.5),
                               (Result) Results.create(37L, 4.2)));
    }
}
