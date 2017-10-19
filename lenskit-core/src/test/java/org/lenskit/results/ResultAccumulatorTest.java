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
import org.lenskit.api.ResultList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ResultAccumulatorTest {
    @Test
    public void testEmptyLimitedAccum() {
        ResultAccumulator acc = ResultAccumulator.create(5);
        assertThat(acc.finish(), hasSize(0));
    }

    @Test
    public void testLimitedAddOne() {
        ResultAccumulator acc = ResultAccumulator.create(5);
        acc.add(1, 3.0);
        ResultList res = acc.finish();
        assertThat(res, hasSize(1));
        assertThat(res.get(0).getId(), equalTo(1L));
        assertThat(res.get(0).getScore(), equalTo(3.0));
    }

    @Test
    public void testLimitedAddThree() {
        ResultAccumulator acc = ResultAccumulator.create(5);
        acc.add(1, 3.0);
        acc.add(2, 2.0);
        acc.add(3, 5.0);
        ResultList res = acc.finish();
        assertThat(res, hasSize(3));
        assertThat(res.idList(), contains(3L, 1L, 2L));
        assertThat(Results.basicCopy(res.get(0)),
                   equalTo(Results.create(3, 5.0)));
        assertThat(Results.basicCopy(res.get(1)),
                   equalTo(Results.create(1, 3.0)));
        assertThat(Results.basicCopy(res.get(2)),
                   equalTo(Results.create(2, 2.0)));
    }

    @Test
    public void testLimitedAddThreeLimit2() {
        ResultAccumulator acc = ResultAccumulator.create(2);
        acc.add(1, 3.0);
        acc.add(2, 2.0);
        acc.add(3, 5.0);
        ResultList res = acc.finish();
        assertThat(res, hasSize(2));
        assertThat(res.idList(), contains(3L, 1L));
        assertThat(Results.basicCopy(res.get(0)),
                   equalTo(Results.create(3, 5.0)));
        assertThat(Results.basicCopy(res.get(1)),
                   equalTo(Results.create(1, 3.0)));
    }

    @Test
    public void testEmptyUnlimitedAccum() {
        ResultAccumulator acc = ResultAccumulator.create(-1);
        assertThat(acc.finish(), hasSize(0));
    }

    @Test
    public void testUnlimitedAddOne() {
        ResultAccumulator acc = ResultAccumulator.create(-1);
        acc.add(1, 3.0);
        ResultList res = acc.finish();
        assertThat(res, hasSize(1));
        assertThat(res.get(0).getId(), equalTo(1L));
        assertThat(res.get(0).getScore(), equalTo(3.0));
    }

    @Test
    public void testUnlimitedAddThree() {
        ResultAccumulator acc = ResultAccumulator.create(-1);
        acc.add(1, 3.0);
        acc.add(2, 2.0);
        acc.add(3, 5.0);
        ResultList res = acc.finish();
        assertThat(res, hasSize(3));
        assertThat(res.idList(), contains(3L, 1L, 2L));
        assertThat(Results.basicCopy(res.get(0)),
                   equalTo(Results.create(3, 5.0)));
        assertThat(Results.basicCopy(res.get(1)),
                   equalTo(Results.create(1, 3.0)));
        assertThat(Results.basicCopy(res.get(2)),
                   equalTo(Results.create(2, 2.0)));
    }
}
