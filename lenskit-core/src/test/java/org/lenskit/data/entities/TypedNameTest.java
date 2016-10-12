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

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.File;

import static org.grouplens.lenskit.util.test.ExtraMatchers.matchesPattern;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TypedNameTest {
    @Test
    public void testBasicField() {
        TypedName<String> attribute = TypedName.create("foo", String.class);
        assertThat(attribute.getName(), equalTo("foo"));
        assertThat(attribute.getType(), equalTo(String.class));
        // check equality to random other object
        assertThat(attribute, not(equalTo((Object) "foo")));

        assertThat(TypedName.create("foo", String.class),
                   sameInstance(attribute));

        assertThat(attribute.toString(), notNullValue());
        assertThat(attribute.toString(), matchesPattern("^TypedName\\[foo: .*\\]$"));
    }

    @Test
    public void testSerialize() {
        TypedName<String> attribute = TypedName.create("foo", String.class);
        assertThat(SerializationUtils.clone(attribute),
                   sameInstance(attribute));
    }

    @Test
    public void testDifferentTypes() {
        TypedName<String> string = TypedName.create("foo", String.class);
        TypedName<File> file = TypedName.create("foo", File.class);
        assertThat(string, not(equalTo((TypedName) file)));
    }

    @Test
    public void testTypeNames() {
        assertThat(TypedName.create("foo", "string"),
                   equalTo(TypedName.create("foo", (Class) String.class)));
        assertThat(TypedName.create("bar", "long"),
                   equalTo(TypedName.create("bar", (Class) Long.class)));
        assertThat(TypedName.create("bar", "int"),
                   equalTo(TypedName.create("bar", (Class) Integer.class)));
        assertThat(TypedName.create("bar", "double"),
                   equalTo(TypedName.create("bar", (Class) Double.class)));
        assertThat(TypedName.create("bar", "real"),
                   equalTo(TypedName.create("bar", (Class) Double.class)));
        assertThat(TypedName.create("bar", "Double"),
                   equalTo(TypedName.create("bar", (Class) Double.class)));
        assertThat(TypedName.create("bar", "Integer"),
                   equalTo(TypedName.create("bar", (Class) Integer.class)));
        assertThat(TypedName.create("bar", "String"),
                   equalTo(TypedName.create("bar", (Class) String.class)));
        assertThat(TypedName.create("file", "java.io.File"),
                   equalTo(TypedName.create("file", (Class) File.class)));
    }

    @Test
    public void testParseString() {
        assertThat(TypedName.create("foo", String.class).parseString("wombat"),
                   equalTo("wombat"));
    }

    @Test
    public void testParseLong() {
        assertThat(TypedName.create("foo", Long.class).parseString("3209"),
                   equalTo(3209L));
    }
}
