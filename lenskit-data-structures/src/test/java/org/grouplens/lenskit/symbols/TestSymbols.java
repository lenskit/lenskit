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
package org.grouplens.lenskit.symbols;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author John Riedl <riedl@cs.umn.edu>
 */
public class TestSymbols {
    @Test
    public void testConstruction() {
        assertSame(Symbol.of("foo"), Symbol.of("foo"));
        assertSame(Symbol.of("bar"), Symbol.of("bar"));
        assertNotSame(Symbol.of("foo"), Symbol.of("bar"));
        assertNotSame(Symbol.of("bar"), Symbol.of("foo"));
    }

    @Test
    public void testEquals() {
	Symbol s1 = Symbol.of("1");
	Symbol s2 = Symbol.of("2");
	Symbol s3 = Symbol.of("3");
	Symbol s4 = Symbol.of("4");
	Symbol s11 = Symbol.of("1");
	Symbol s12 = Symbol.of("1");
	assertEquals(s1, s11);
	assertEquals(s11, s12);
	assertSame(s1, s11);
	assertSame(s1, s12);
    }

    @Test public void testToString() {
	Symbol sbar = Symbol.of("bar");
	Symbol sfoo = Symbol.of("foo");
	assertEquals(Symbol.of("foo").toString(), "Symbol based on name 'foo'");
	assertEquals(sbar.toString(), "Symbol based on name 'bar'");
	assertEquals(sfoo.toString(), "Symbol based on name 'foo'");
    }
}
