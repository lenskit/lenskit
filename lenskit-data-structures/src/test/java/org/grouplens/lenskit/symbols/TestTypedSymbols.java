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

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestTypedSymbols {
    @Test
    public void testConstruction() {
        assertSame(TypedSymbol.of("foo", String.class), TypedSymbol.of("foo", String.class));
        assertSame(TypedSymbol.of("foo", Integer.class), TypedSymbol.of("foo", Integer.class));
        assertNotSame(TypedSymbol.of("foo", String.class), TypedSymbol.of("foo", Integer.class));
        assertSame(TypedSymbol.of("bar", String.class), TypedSymbol.of("bar", String.class));
        assertNotSame(TypedSymbol.of("foo", String.class), TypedSymbol.of("bar", String.class));
        assertNotSame(TypedSymbol.of("bar", String.class), TypedSymbol.of("foo", Integer.class));
    }

    @Test
    public void testEquals() {
        TypedSymbol<String> ss11 = TypedSymbol.of("1", String.class);
        TypedSymbol<String> ss12 = TypedSymbol.of("1", String.class);
        TypedSymbol<String> ss21 = TypedSymbol.of("2", String.class);
        TypedSymbol<String> ss22 = TypedSymbol.of("2", String.class);
        TypedSymbol<Integer> si1 = TypedSymbol.of("1", Integer.class);
        TypedSymbol<Integer> si2 = TypedSymbol.of("1", Integer.class);
        assertTrue(ss11.equals(ss12));
        assertTrue(ss21.equals(ss22));
        assertTrue(si1.equals(si2));
        assertFalse(ss11.equals(ss21));
        assertFalse(ss11.equals(si1));
        
    }

    @Test
    public void testGetName() {
        assertEquals("foo", TypedSymbol.of("foo", String.class).getName());
        assertEquals("bar", TypedSymbol.of("bar", String.class).getName());
    }
    
    @Test
    public void testGetType() {
        assertEquals(String.class, TypedSymbol.of("foo", String.class).getType());
        assertEquals(Integer.class, TypedSymbol.of("foo", Integer.class).getType());
    }
    
    @Test
    public void testToString() {
        TypedSymbol<String> sbar = TypedSymbol.of("bar", String.class);
        TypedSymbol<Integer> sfoo = TypedSymbol.of("foo", Integer.class);
        assertEquals("TypedSymbol.of(bar,String)", sbar.toString());
        assertEquals("TypedSymbol.of(foo,Integer)", sfoo.toString());
        
    }
}
