/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
