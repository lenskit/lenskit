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
package org.lenskit.data.dao;

import org.junit.Test;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

/**
 * Tests for the entity query builder.  Some of its functionality is tested in {@link EntityQueryTest}, but this tests
 * additional builder features.
 */
public class EntityQueryBuilderTest {
    @Test
    public void testCopyEmpty() {
        EntityQueryBuilder eqb = EntityQuery.newBuilder(CommonTypes.ITEM);
        EntityQueryBuilder copy = eqb.copy();
        EntityQuery<Entity> q = copy.setEntityType(CommonTypes.USER).build();
        assertThat(q, notNullValue());
        assertThat(q.getEntityType(), equalTo(CommonTypes.USER));

        EntityQuery<Entity> q2 = eqb.build();
        assertThat(q2, notNullValue());
        assertThat(q2.getEntityType(), equalTo(CommonTypes.ITEM));
    }
}
