/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ScoredIdBuilderTest {

    private final Symbol fooSym = Symbol.of("foo");
    private final TypedSymbol<Integer> fooIntSym = TypedSymbol.of(Integer.class, "foo");
    
    @Test
    public void testScoredIdBuilder() {
        ScoredIdBuilder sib = new ScoredIdBuilder();
        assertEquals(new ScoredIdImpl(0, 0), sib.build());
    }

    @Test
    public void testScoredIdBuilderLong() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1);
        assertEquals(new ScoredIdImpl(1, 0), sib.build());
    }

    @Test
    public void testScoredIdBuilderLongDouble() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(new ScoredIdImpl(1, 2), sib.build());
    }

    @Test
    public void testSetId() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.setId(2));
        assertEquals(new ScoredIdImpl(2, 2), sib.build());
    }

    @Test
    public void testSetScore() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.setScore(3));
        assertEquals(new ScoredIdImpl(1, 3), sib.build());
    }

    @Test
    public void testAddChannel() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.addChannel(fooSym, 1.0));
        ScoredId id = sib.build();
        assertThat(id.getId(), equalTo(1L));
        assertThat(id.getScore(), equalTo(2.0));
        assertThat(id.hasChannel(fooSym.withType(Double.class)), equalTo(true));
        assertThat(id.getUnboxedChannelValue(fooSym), equalTo(1.0));
    }

    @Test
    public void testAddTypedChannel() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.addChannel(fooIntSym, 1));
        ScoredId id = sib.build();
        assertThat(id.getId(), equalTo(1L));
        assertThat(id.getScore(), equalTo(2.0));
        assertThat(id.hasChannel(fooIntSym), equalTo(true));
        assertThat(id.getChannelValue(fooIntSym), equalTo(1));
        assertThat(id.getChannels(),
                   contains((SymbolValue) SymbolValue.of(fooIntSym, 1)));
    }

    @Test
    public void testClearChannels() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.addChannel(fooSym, 1.0));
        assertEquals(sib, sib.addChannel(fooIntSym, 1));
        assertEquals(sib, sib.clearChannels());
        ScoredId id = sib.build();
        assertThat(id.getChannels(), hasSize(0));
        assertThat(id.hasChannel(fooIntSym), equalTo(false));
        assertThat(id.hasUnboxedChannel(fooSym), equalTo(false));
    }

    @Test
    public void testStateness() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        assertEquals(sib, sib.addChannel(fooSym, 1.0));
        assertEquals(sib, sib.addChannel(fooIntSym, 1));
        ScoredId sid = sib.build();
        sib.setId(3);
        sib.setScore(4);
        sib.addChannel(fooSym, 2.0);
        sib.addChannel(fooIntSym, 2);
        assertThat(sid.getChannels(), hasSize(2));
        sib.clearChannels();
        assertThat(sid.getChannels(), hasSize(2));
    }

    @Test
    public void testCopy() {
        ScoredIdBuilder sib = new ScoredIdBuilder(1, 2);
        sib.addChannel(fooSym, 1.0);
        sib.addChannel(fooIntSym, 1);
        ScoredId id = sib.build();
        ScoredId id2 = ScoredIds.copyBuilder(id).build();
        assertThat(id2, equalTo(id));

        id2 = ScoredIds.copyBuilder(id).setScore(2.5).build();
        assertThat(id2, not(equalTo(id)));
        assertThat(id2.getScore(), equalTo(2.5));
    }
}
