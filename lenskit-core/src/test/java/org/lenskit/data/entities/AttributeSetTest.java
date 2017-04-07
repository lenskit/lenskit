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
package org.lenskit.data.entities;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
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
}
