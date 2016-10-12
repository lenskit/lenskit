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
