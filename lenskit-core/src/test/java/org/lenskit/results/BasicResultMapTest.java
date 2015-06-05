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
import org.lenskit.api.ResultMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicResultMapTest {
    @Test
    public void testEmptyMap() {
        ResultMap r = Results.newResultMap();
        assertThat(r.isEmpty(), equalTo(true));
        assertThat(r.size(), equalTo(0));
        assertThat(r.resultSet(), hasSize(0));
        assertThat(r.scoreMap().size(), equalTo(0));
    }

    @Test
    public void testSingletonMap() {
        ResultMap r = Results.<Result>newResultMap(Results.create(42L, 3.5));
        assertThat(r.size(), equalTo(1));
        assertThat(r.resultSet(), contains((Result) Results.create(42L, 3.5)));
        assertThat(r.resultSet(), hasSize(1));
        assertThat(r.get(42L), equalTo((Result) Results.create(42L, 3.5)));
    }

    @Test
    public void testMultiMap() {
        ResultMap r = Results.<Result>newResultMap(Results.create(42L, 3.5),
                                                   Results.create(37L, 4.2));
        assertThat(r.size(), equalTo(2));
        assertThat(r.keySet(), contains(42L, 37L));
        assertThat(r.resultSet(), contains((Result) Results.create(42L, 3.5),
                                           (Result) Results.create(37L, 4.2)));
    }
}
