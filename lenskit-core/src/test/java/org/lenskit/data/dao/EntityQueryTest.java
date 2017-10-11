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
