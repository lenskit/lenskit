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
 * @author John Riedl <riedl@cs.umn.edu>
 */
public class TestSymbols {
    @Test
    public void testConstruction() {
        assertThat(Symbol.of("foo"), sameInstance(Symbol.of("foo")));
        assertThat(Symbol.of("bar"), sameInstance(Symbol.of("bar")));
        assertThat(Symbol.of("foo"), not(sameInstance(Symbol.of("bar"))));
        assertThat(Symbol.of("bar"), not(sameInstance(Symbol.of("foo"))));
    }

    @Test
    public void testEquals() {
        Symbol s1 = Symbol.of("1");
        Symbol s2 = Symbol.of("2");
        Symbol s11 = Symbol.of("1");
        Symbol s12 = Symbol.of("1");
        Symbol s22 = Symbol.of("2");
        assertThat(s1, equalTo(s11));
        assertThat(s11, equalTo(s12));
        assertThat(s1, sameInstance(s11));
        assertThat(s1, sameInstance(s12));
        assertThat(s2, equalTo(s22));
        assertThat(s2, equalTo(s22));
    }

    @Test
    public void testToString() {
        Symbol sbar = Symbol.of("bar");
        Symbol sfoo = Symbol.of("foo");
        assertThat(Symbol.of("foo").toString(), equalTo("Symbol.of(foo)"));
        assertThat(sbar.toString(), equalTo("Symbol.of(bar)"));
        assertThat(sfoo.toString(), equalTo("Symbol.of(foo)"));
    }
}
