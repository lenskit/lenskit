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
package org.grouplens.lenskit.util;

import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.grouplens.lenskit.util.TypeUtils.resolveTypeName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
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
    public void testResolveStringType() {
        assertThat(resolveTypeName("string"), equalTo(String.class));
        assertThat(resolveTypeName("String"), equalTo(String.class));
    }

    @Test
    public void testResolveNumericTypes() {
        assertThat(resolveTypeName("double"), equalTo(Double.class));
        assertThat(resolveTypeName("Double"), equalTo(Double.class));
        assertThat(resolveTypeName("long"), equalTo(Long.class));
        assertThat(resolveTypeName("Long"), equalTo(Long.class));
        assertThat(resolveTypeName("int"), equalTo(Integer.class));
        assertThat(resolveTypeName("Integer"), equalTo(Integer.class));
        assertThat(resolveTypeName("real"), equalTo(Double.class));
    }

    @Test
    public void testResolveClassType() {
        assertThat(resolveTypeName("java.io.File"), equalTo(File.class));
    }
}
