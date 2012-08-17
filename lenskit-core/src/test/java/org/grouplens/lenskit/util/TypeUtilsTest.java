/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import static org.grouplens.lenskit.util.TypeUtils.isSubclass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class TypeUtilsTest {

    @Test
    public void testIsSubclass() {
        Predicate<Class<?>> p = isSubclass(Collection.class);
        assertTrue(p.apply(Collection.class));
        assertTrue(p.apply(List.class));
        assertTrue(p.apply(LinkedList.class));
        assertFalse(p.apply(URL.class));
        assertFalse(p.apply(Element.class));
    }

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
}
