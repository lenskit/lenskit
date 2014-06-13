/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.scored;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ScoredIdComparatorTest {
    @Test
    public void testIdComparator() {
        ScoredId low = new ScoredIdBuilder(1, 3.5).build();
        ScoredId high = new ScoredIdBuilder(2, 3.5).build();
        Ordering<ScoredId> comp = ScoredIds.idOrder();
        assertThat(comp.compare(low, low), equalTo(0));
        assertThat(comp.compare(low, high), lessThan(0));
        assertThat(comp.compare(high, low), greaterThan(0));
    }

    @Test
    public void testScoreComparator() {
        ScoredId low = new ScoredIdBuilder(1, 2.5).build();
        ScoredId high = new ScoredIdBuilder(2, 3.5).build();
        Ordering<ScoredId> comp = ScoredIds.scoreOrder();
        assertThat(comp.compare(low, low), equalTo(0));
        assertThat(comp.compare(low, high), lessThan(0));
        assertThat(comp.compare(high, low), greaterThan(0));
    }

    @Test
    public void testChannelComparator() {
        Symbol foo = Symbol.of("foo");
        ScoredId low = new ScoredIdBuilder(1, 5).addChannel(foo, 2.5).build();
        ScoredId high = new ScoredIdBuilder(2, 5).addChannel(foo, 3.5).build();
        ScoredId missing = new ScoredIdBuilder(3, 5).build();
        Ordering<ScoredId> comp = ScoredIds.channelOrder(foo);
        assertThat(comp.compare(low, low), equalTo(0));
        assertThat(comp.compare(low, high), lessThan(0));
        assertThat(comp.compare(high, low), greaterThan(0));
        assertThat(comp.compare(missing, missing), equalTo(0));
        assertThat(comp.compare(missing, low), lessThan(0));
        assertThat(comp.compare(low, missing), greaterThan(0));
    }

    @Test
    public void testTypedChannelComparator() {
        TypedSymbol<String> foo = TypedSymbol.of(String.class, "foo");
        ScoredId low = new ScoredIdBuilder(1, 5).addChannel(foo, "hello").build();
        ScoredId high = new ScoredIdBuilder(2, 5).addChannel(foo, "wombat").build();
        ScoredId missing = new ScoredIdBuilder(3, 5).build();
        Ordering<ScoredId> comp = ScoredIds.channelOrder(foo);
        assertThat(comp.compare(low, low), equalTo(0));
        assertThat(comp.compare(low, high), lessThan(0));
        assertThat(comp.compare(high, low), greaterThan(0));
        assertThat(comp.compare(missing, missing), equalTo(0));
        assertThat(comp.compare(missing, low), lessThan(0));
        assertThat(comp.compare(low, missing), greaterThan(0));
    }

    @Test
    public void testTypedChannelCustomComparator() {
        TypedSymbol<String> foo = TypedSymbol.of(String.class, "foo");
        ScoredId low = new ScoredIdBuilder(1, 5).addChannel(foo, "hello").build();
        ScoredId low2 = new ScoredIdBuilder(1, 5).addChannel(foo, "HeLLo").build();
        ScoredId high = new ScoredIdBuilder(2, 5).addChannel(foo, "wombat").build();
        Ordering<ScoredId> comp = ScoredIds.channelOrder(foo, String.CASE_INSENSITIVE_ORDER);
        assertThat(comp.compare(low, low), equalTo(0));
        assertThat(comp.compare(low, low2), equalTo(0));
        assertThat(comp.compare(low, high), lessThan(0));
        assertThat(comp.compare(low2, high), lessThan(0));
        assertThat(comp.compare(high, low), greaterThan(0));
        assertThat(comp.compare(high, low2), greaterThan(0));
    }
}
