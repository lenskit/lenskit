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

import com.google.common.reflect.TypeToken;
import org.joda.convert.FromStringConverter;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.util.TypeUtils.*;

public class TypeUtilsTest {

    @Test
    public void testTypeClosure() {
        Set<Class<?>> closure = TypeUtils.typeClosure(SortedSet.class);
        assertTrue(closure.contains(SortedSet.class));
        assertTrue(closure.contains(Set.class));
        assertTrue(closure.contains(Collection.class));
        assertTrue(closure.contains(Iterable.class));
        //assertTrue(closure.contains(Object.class));
        assertFalse(closure.contains(List.class));
        assertFalse(closure.contains(null));
    }

    @Test
    public void testResolveTextType() {
        assertThat(resolveTypeName("text"),
                   equalTo((TypeToken) TypeToken.of(Text.class)));
        assertThat(resolveTypeName("Text"),
                   equalTo((TypeToken) TypeToken.of(Text.class)));
    }

    @Test
    public void testResolveStringType() {
        assertThat(resolveTypeName("string"),
                   equalTo((TypeToken) TypeToken.of(String.class)));
        assertThat(resolveTypeName("String"),
                   equalTo((TypeToken) TypeToken.of(String.class)));
    }

    @Test
    public void testResolveNumericTypes() {
        assertThat(resolveTypeName("double"),
                   equalTo((TypeToken) TypeToken.of(Double.class)));
        assertThat(resolveTypeName("Double"),
                   equalTo((TypeToken) TypeToken.of(Double.class)));
        assertThat(resolveTypeName("long"),
                   equalTo((TypeToken) TypeToken.of(Long.class)));
        assertThat(resolveTypeName("Long"),
                   equalTo((TypeToken) TypeToken.of(Long.class)));
        assertThat(resolveTypeName("int"),
                   equalTo((TypeToken) TypeToken.of(Integer.class)));
        assertThat(resolveTypeName("Integer"),
                   equalTo((TypeToken) TypeToken.of(Integer.class)));
        assertThat(resolveTypeName("real"),
                   equalTo((TypeToken) TypeToken.of(Double.class)));
    }

    @Test
    public void testResolveClassType() {
        assertThat(resolveTypeName("java.io.File"),
                   equalTo((TypeToken) TypeToken.of(File.class)));
    }

    @Test
    public void testStringifyBasicTypes() {
        assertThat(makeTypeName(TypeToken.of(String.class)),
                   equalTo("string"));
        assertThat(makeTypeName(TypeToken.of(Long.class)),
                   equalTo("long"));
        assertThat(makeTypeName(TypeToken.of(File.class)),
                   equalTo("java.io.File"));
        assertThat(makeTypeName(TypeToken.of(Integer.class)),
                   equalTo("int"));
        assertThat(makeTypeName(TypeToken.of(Double.class)),
                   equalTo("double"));
    }

    @Test
    public void testParseListType() {
        TypeToken<List<String>> strList = new TypeToken<List<String>>() {};
        assertThat(resolveTypeName("string[]"),
                   equalTo((TypeToken) strList));
        assertThat(resolveTypeName("int[]"),
                   equalTo((TypeToken) new TypeToken<List<Integer>>(){}));
        assertThat(resolveTypeName("int[][]"),
                   equalTo((TypeToken) new TypeToken<List<List<Integer>>>(){}));
    }

    @Test
    public void testStringifyListType() {
        TypeToken<List<String>> strList = new TypeToken<List<String>>() {};
        assertThat(makeTypeName(strList), equalTo("string[]"));
        assertThat(makeTypeName(new TypeToken<List<List<Double>>>() {}),
                   equalTo("double[][]"));
    }

    @Test
    public void testExtractListElement() {
        TypeToken<List<String>> strList = new TypeToken<List<String>>() {};
        assertThat(listElementType(strList),
                   equalTo(TypeToken.of(String.class)));

        TypeToken<List<List<File>>> nestedList = new TypeToken<List<List<File>>>() {};
        assertThat(listElementType(nestedList),
                   equalTo(makeListType(TypeToken.of(File.class))));
    }

    @Test
    public void testBasicFromString() {
        TypeToken<Integer> intType = TypeToken.of(Integer.class);
        FromStringConverter<Integer> cvt = TypeUtils.lookupFromStringConverter(intType);
        assertThat(cvt, notNullValue());
        assertThat(cvt.convertFromString((Class) intType.getRawType(), "1348"),
                   equalTo(1348));
    }

    @Test
    public void testStringListFromString() {
        TypeToken<List<String>> tok = new TypeToken<List<String>>() {};
        FromStringConverter<List<String>> cvt = TypeUtils.lookupFromStringConverter(tok);
        assertThat(cvt, notNullValue());
        List<String> result = cvt.convertFromString((Class) List.class, "foo,bar,\"hamster,salad\"");
        assertThat(result, contains("foo", "bar", "hamster,salad"));
    }

    @Test
    public void testIntListFromString() {
        TypeToken<List<Integer>> tok = new TypeToken<List<Integer>>() {};
        FromStringConverter<List<Integer>> cvt = TypeUtils.lookupFromStringConverter(tok);
        assertThat(cvt, notNullValue());
        List<Integer> result = cvt.convertFromString((Class) List.class, "39,42");
        assertThat(result, contains(39, 42));
    }
}

