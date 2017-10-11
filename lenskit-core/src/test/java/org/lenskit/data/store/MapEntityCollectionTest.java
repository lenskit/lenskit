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
package org.lenskit.data.store;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * Created by MichaelEkstrand on 6/18/2016.
 */
public class MapEntityCollectionTest {
    @Test
    public void testEmptyBuilder() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER)
                                              .build();
        assertThat(ec.size(), equalTo(0));
        assertThat(ec.lookup(42), nullValue());
        assertThat(ec.getType(), equalTo(CommonTypes.USER));
        assertThat(ec.idSet(), hasSize(0));
    }

    @Test
    public void testAddEntity() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER)
                                              .add(Entities.create(CommonTypes.USER, 42))
                                              .build();
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(42),
                   equalTo(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.lookup(70), nullValue());
        assertThat(Lists.newArrayList(ec),
                   contains(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.find(CommonAttributes.ENTITY_ID, 42L),
                   contains(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.getType(), equalTo(CommonTypes.USER));
        assertThat(ec.idSet(), contains(42L));
    }

    @Test
    public void testFindEntity() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .add(rating)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }

    @Test
    public void testFindIndexedEntity() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .addIndex(CommonAttributes.USER_ID)
                                              .add(rating)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }

    @Test
    public void testIndexEntityAfterAddStarted() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .add(rating)
                                              .addIndex(CommonAttributes.USER_ID)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }
}
