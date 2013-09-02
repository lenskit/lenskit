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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SymbolValueTest {
    public Symbol RAW_SYM = Symbol.of("foo");
    public TypedSymbol<Double> BOXED_SYM = TypedSymbol.of(Double.class, "foo");
    public TypedSymbol<String> STRING_SYM = TypedSymbol.of(String.class, "bar");

    @Test
    public void testTypedValue() {
        SymbolValue<String> sv = SymbolValue.of(STRING_SYM, "hello");
        assertThat(sv, notNullValue());
        assertThat(sv.getSymbol(), equalTo(STRING_SYM));
        assertThat(sv.getRawSymbol(), equalTo(Symbol.of("bar")));
        assertThat(sv.getValue(), equalTo("hello"));
    }

    @Test
    public void testUnboxedValue() {
        DoubleSymbolValue sv = SymbolValue.of(BOXED_SYM, 3.5);
        assertThat(sv, notNullValue());
        assertThat(sv.getSymbol(), equalTo(BOXED_SYM));
        assertThat(sv.getRawSymbol(), equalTo(RAW_SYM));
        assertThat(sv.getDoubleValue(), equalTo(3.5));
        assertThat(sv.getValue(), equalTo(3.5));
    }

    @Test
    public void testUnboxedFromRawSymbol() {
        DoubleSymbolValue sv = SymbolValue.of(RAW_SYM, 3.5);
        assertThat(sv, notNullValue());
        assertThat(sv.getSymbol(), equalTo(BOXED_SYM));
        assertThat(sv.getRawSymbol(), equalTo(RAW_SYM));
        assertThat(sv.getDoubleValue(), equalTo(3.5));
        assertThat(sv.getValue(), equalTo(3.5));
    }

    @Test
    public void testAutoUnboxed() {
        SymbolValue<Double> sv = SymbolValue.of(BOXED_SYM, Double.valueOf(3.5));
        assertThat(sv, notNullValue());
        assertThat(sv, instanceOf(DoubleSymbolValue.class));
        assertThat(sv.getSymbol(), equalTo(BOXED_SYM));
        assertThat(sv.getRawSymbol(), equalTo(RAW_SYM));
        assertThat(sv.getValue(), equalTo(3.5));
    }

    @Test
    public void testEquals() {
        SymbolValue<String> base = STRING_SYM.withValue("foo");
        SymbolValue<String> eq = STRING_SYM.withValue("foo");
        SymbolValue<String> neq = STRING_SYM.withValue("bar");
        SymbolValue<String> dsym = Symbol.of("foo").withType(String.class).withValue("foo");
        SymbolValue dclass = Symbol.of("bar").withType(List.class).withValue(Collections.emptyList());

        assertThat(eq, equalTo(base));
        assertThat(neq, not(equalTo(base)));
        assertThat(dsym, not(equalTo(base)));
        assertThat(dclass, not(equalTo((SymbolValue) base)));
    }

    @Test
    public void testHasSymbol() {
        assertThat(SymbolValue.hasSymbol(STRING_SYM).apply(SymbolValue.of(STRING_SYM, "foo")),
                   equalTo(true));
        assertThat(SymbolValue.hasSymbol(BOXED_SYM).apply(SymbolValue.of(STRING_SYM, "foo")),
                   equalTo(false));
    }
}
