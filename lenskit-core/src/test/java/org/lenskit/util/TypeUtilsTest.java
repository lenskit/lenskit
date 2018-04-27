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

