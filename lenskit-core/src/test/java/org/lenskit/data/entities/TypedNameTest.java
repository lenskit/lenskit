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

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.grouplens.lenskit.util.test.ExtraMatchers.matchesPattern;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TypedNameTest {
    @Test
    public void testBasicField() {
        TypedName<String> attribute = TypedName.create("foo", String.class);
        assertThat(attribute.getName(), equalTo("foo"));
        assertThat(attribute.getType(), equalTo(TypeToken.of(String.class)));
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

    @Test
    public void testParseStringList() {
        TypeToken<List<String>> ltt = new TypeToken<List<String>>() {};
        TypedName<List<String>> name = TypedName.create("tags", ltt);
        assertThat(name.parseString("foo,bar"),
                   contains("foo", "bar"));
    }
}
