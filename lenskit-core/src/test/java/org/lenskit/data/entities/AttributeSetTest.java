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
package org.lenskit.data.entities;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Attribute set.
 */
public class AttributeSetTest {
    @Test
    public void testBasicSingleAttribute() {
        AttributeSet attrs = AttributeSet.create(CommonAttributes.ENTITY_ID);
        assertThat(attrs.size(), equalTo(1));
        assertThat(attrs.lookup(CommonAttributes.ENTITY_ID),
                   equalTo(0));
        assertThat(attrs.lookup(CommonAttributes.ITEM_ID),
                   equalTo(-1));
        assertThat(attrs, contains(CommonAttributes.ENTITY_ID));
        assertThat(attrs.contains(CommonAttributes.ENTITY_ID), equalTo(true));
        assertThat(attrs.contains(CommonAttributes.NAME), equalTo(false));
    }

    @Test
    public void testLookupByName() {
        AttributeSet attrs = AttributeSet.create(CommonAttributes.ENTITY_ID);
        assertThat(attrs.lookup("id"),
                   equalTo(0));
        assertThat(attrs.lookup("foobat"),
                   equalTo(-1));
    }

    @Test
    public void testNameSet() {
        AttributeSet attrs = AttributeSet.create(CommonAttributes.ENTITY_ID);
        assertThat(attrs.nameSet(), contains("id"));
        assertThat(attrs.nameSet().contains("id"),
                   equalTo(true));
        assertThat(attrs.nameSet().contains("foo"),
                   equalTo(false));
    }

    @Test
    public void testInternCache() {
        AttributeSet attrs = AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                 CommonAttributes.USER_ID,
                                                 CommonAttributes.ITEM_ID);
        AttributeSet a2 = AttributeSet.create(CommonAttributes.ENTITY_ID,
                                              CommonAttributes.USER_ID,
                                              CommonAttributes.ITEM_ID);
        assertThat(a2, sameInstance(attrs));
    }

    @Test
    public void testMultiple() {
        AttributeSet attrs = AttributeSet.create(TypedName.create("abbr", String.class),
                                                 CommonAttributes.ENTITY_ID,
                                                 CommonAttributes.USER_ID);
        assertThat(attrs.nameSet(), contains("id", "abbr", "user"));
    }
}
