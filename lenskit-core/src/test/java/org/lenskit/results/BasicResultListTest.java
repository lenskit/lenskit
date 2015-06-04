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

import org.junit.Test;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultListTest {
    @Test
    public void testEmptyList() {
        ResultList<Result> r = Results.newResultList();
        assertThat(r.isEmpty(), equalTo(true));
        assertThat(r.size(), equalTo(0));
        assertThat(r.idList(), hasSize(0));
    }

    @Test
    public void testSingletonList() {
        ResultList<Result> r = Results.<Result>newResultList(Results.create(42L, 3.5));
        assertThat(r, hasSize(1));
        assertThat(r, contains((Result) Results.create(42L, 3.5)));
        assertThat(r.idList(), hasSize(1));
        assertThat(r.idList(), contains(42L));
    }

    @Test
    public void testMultiList() {
        ResultList<Result> r = Results.<Result>newResultList(Results.create(42L, 3.5),
                                                             Results.create(37L, 4.2));
        assertThat(r, hasSize(2));
        assertThat(r, contains((Result) Results.create(42L, 3.5),
                               (Result) Results.create(37L, 4.2)));
    }
}
