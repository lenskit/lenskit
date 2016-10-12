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

import org.hamcrest.Matcher;
import org.junit.Test;
import org.lenskit.data.entities.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EntityQueryTest {
    @Test
    public void testUniversalQuery() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.ITEM).build();
        assertThat(q.getEntityType(), equalTo(CommonTypes.ITEM));
        assertThat(q.getFilterFields(), hasSize(0));
        assertThat(q.getSortKeys(), hasSize(0));
        assertThat(q.getViewType(), equalTo(Entity.class));
    }

    @Test
    public void testSingleAttrFilter() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.RATING)
                                           .addFilterField(CommonAttributes.ITEM_ID, 42L)
                                           .build();
        assertThat(q.getEntityType(), equalTo(CommonTypes.RATING));
        assertThat(q.getFilterFields(),
                   (Matcher) contains(Attribute.create(CommonAttributes.ITEM_ID, 42L)));
        assertThat(q.getSortKeys(), hasSize(0));
        assertThat(q.getViewType(), equalTo(Entity.class));
    }

    @Test
    public void testTwoAttrFilter() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.RATING)
                                           .addFilterField(CommonAttributes.ITEM_ID, 42L)
                                           .addFilterField(CommonAttributes.USER_ID, 29L)
                                           .build();
        assertThat(q.getEntityType(), equalTo(CommonTypes.RATING));
        assertThat(q.getFilterFields(),
                   (Matcher) containsInAnyOrder(Attribute.create(CommonAttributes.ITEM_ID, 42L),
                                                Attribute.create(CommonAttributes.USER_ID, 29L)));
        assertThat(q.getSortKeys(), hasSize(0));
        assertThat(q.getViewType(), equalTo(Entity.class));
    }

    @Test
    public void testSortKey() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.RATING)
                                           .addSortKey(CommonAttributes.ITEM_ID)
                                           .build();
        assertThat(q.getEntityType(), equalTo(CommonTypes.RATING));
        assertThat(q.getFilterFields(), hasSize(0));
        assertThat(q.getSortKeys(),
                   contains(new SortKey(CommonAttributes.ITEM_ID, SortOrder.ASCENDING)));
        assertThat(q.getViewType(),
                   equalTo(Entity.class));
    }

    @Test
    public void testUniversalMatch() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.ITEM)
                                           .build();
        assertThat(q.matches(Entities.create(CommonTypes.ITEM, 42L)),
                   equalTo(true));
        assertThat(q.matches(Entities.create(CommonTypes.USER, 42L)),
                   equalTo(false));
    }

    @Test
    public void testAttributeMatch() {
        EntityQuery<Entity> q = EntityQuery.newBuilder(CommonTypes.RATING)
                                           .addFilterField(CommonAttributes.USER_ID, 42L)
                                           .build();
        assertThat(q.matches(Entities.create(CommonTypes.RATING, 42L)),
                   equalTo(false));
        assertThat(q.matches(Entities.create(CommonTypes.USER, 42L)),
                   equalTo(false));
        assertThat(q.matches(Entities.newBuilder(CommonTypes.RATING, 42L)
                                     .setAttribute(CommonAttributes.USER_ID, 42L)
                                     .build()),
                   equalTo(true));
        assertThat(q.matches(Entities.newBuilder(CommonTypes.RATING, 42L)
                                     .setAttribute(CommonAttributes.USER_ID, 78L)
                                     .build()),
                   equalTo(false));
    }
}
