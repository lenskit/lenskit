/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.symbols;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestTypedSymbols {
    @Test
    public void testConstruction() {
        assertSame(TypedSymbol.of(String.class, "foo"), TypedSymbol.of(String.class, "foo"));
        assertSame(TypedSymbol.of(Integer.class, "foo"), TypedSymbol.of(Integer.class, "foo"));
        assertNotSame(TypedSymbol.of(String.class, "foo"), TypedSymbol.of(Integer.class, "foo"));
        assertSame(TypedSymbol.of(String.class, "bar"), TypedSymbol.of(String.class, "bar"));
        assertNotSame(TypedSymbol.of(String.class, "foo"), TypedSymbol.of(String.class, "bar"));
        assertNotSame(TypedSymbol.of(String.class, "bar"), TypedSymbol.of(Integer.class, "foo"));
    }

    @Test
    public void testEquals() {
        TypedSymbol<String> ss11 = TypedSymbol.of(String.class, "1");
        TypedSymbol<String> ss12 = TypedSymbol.of(String.class, "1");
        TypedSymbol<String> ss21 = TypedSymbol.of(String.class, "2");
        TypedSymbol<String> ss22 = TypedSymbol.of(String.class, "2");
        TypedSymbol<Integer> si1 = TypedSymbol.of(Integer.class, "1");
        TypedSymbol<Integer> si2 = TypedSymbol.of(Integer.class, "1");
        assertTrue(ss11.equals(ss12));
        assertTrue(ss21.equals(ss22));
        assertTrue(si1.equals(si2));
        assertFalse(ss11.equals(ss21));
        assertFalse(ss11.equals(si1));
    }

    @Test
    public void testGetName() {
        assertEquals("foo", TypedSymbol.of(String.class, "foo").getName());
        assertEquals("bar", TypedSymbol.of(String.class, "bar").getName());
    }
    
    @Test
    public void testGetType() {
        assertEquals(String.class, TypedSymbol.of(String.class, "foo").getType());
        assertEquals(Integer.class, TypedSymbol.of(Integer.class, "foo").getType());
    }
    
    @Test
    public void testToString() {
        TypedSymbol<String> sbar = TypedSymbol.of(String.class, "bar");
        TypedSymbol<Integer> sfoo = TypedSymbol.of(Integer.class, "foo");
        assertEquals("TypedSymbol.of(String,bar)", sbar.toString());
        assertEquals("TypedSymbol.of(Integer,foo)", sfoo.toString());
    }

    @Test
    public void testSerialize() {
        TypedSymbol<InputStream> sbar = TypedSymbol.of(InputStream.class, "ratings");
        TypedSymbol<InputStream> cloned = SerializationUtils.clone(sbar);
        assertThat(cloned, sameInstance(sbar));
    }

    @Test
    public void testPrimitive() {
        TypedSymbol<Double> boxed = TypedSymbol.of(Double.class, "symbol");
        TypedSymbol<Double> unboxed = TypedSymbol.of(double.class, "symbol");
        assertThat(unboxed, equalTo(boxed));
        assertThat(unboxed, sameInstance(boxed));
    }

    @Test
    public void testWithType() {
        Symbol foo = Symbol.of("foo");
        TypedSymbol<String> s1 = foo.withType(String.class);
        TypedSymbol<String> s2 = foo.withType(String.class);
        assertThat(s2, equalTo(s1));
        assertThat(s2, sameInstance(s1));
    }
}
