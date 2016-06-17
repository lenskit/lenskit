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
package org.lenskit.data.entities;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.File;

import static org.grouplens.lenskit.util.test.ExtraMatchers.matchesPattern;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AttributeTest {
    @Test
    public void testBasicField() {
        Attribute<String> attribute = Attribute.create("foo", String.class);
        assertThat(attribute.getName(), equalTo("foo"));
        assertThat(attribute.getType(), equalTo(String.class));
        // check equality to random other object
        assertThat(attribute, not(equalTo((Object) "foo")));

        assertThat(Attribute.create("foo", String.class),
                   sameInstance(attribute));

        assertThat(attribute.toString(), notNullValue());
        assertThat(attribute.toString(), matchesPattern("^Attribute\\[foo, type=.*\\]$"));
    }

    @Test
    public void testSerialize() {
        Attribute<String> attribute = Attribute.create("foo", String.class);
        assertThat(SerializationUtils.clone(attribute),
                   sameInstance(attribute));
    }

    @Test
    public void testDifferentTypes() {
        Attribute<String> string = Attribute.create("foo", String.class);
        Attribute<File> file = Attribute.create("foo", File.class);
        assertThat(string, not(equalTo((Attribute) file)));
    }
}
