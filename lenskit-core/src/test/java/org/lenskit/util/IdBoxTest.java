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
package org.lenskit.util;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class IdBoxTest {
    @Test
    public void testBasicBoxBehavior() {
        IdBox<String> box = IdBox.create(42, "foo");
        assertThat(box, notNullValue());
        assertThat(box.getId(), equalTo(42L));
        assertThat(box.getValue(), equalTo("foo"));
    }

    @Test
    public void testEquality() {
        IdBox<String> box = IdBox.create(42, "foo");
        assertThat(box.equals(null), equalTo(false));
        assertThat(box.equals(IdBox.create(42, "foo")),
                   equalTo(true));
        assertThat(box.equals(IdBox.create(42, "bar")),
                   equalTo(false));
        assertThat(box.equals(IdBox.create(20, "foo")),
                   equalTo(false));
    }
}
