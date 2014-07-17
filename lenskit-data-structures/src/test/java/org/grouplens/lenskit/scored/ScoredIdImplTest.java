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

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ScoredIdImplTest {

    private final Symbol fooSym = Symbol.of("foo");
    private final Symbol barSym = Symbol.of("bar");
    private final TypedSymbol<Integer> fooIntSym = TypedSymbol.of(Integer.class, "foo");
    private final TypedSymbol<String> barStrSym = TypedSymbol.of(String.class, "bar");
    
    
    @Test
    public void testConstructors() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10);
        ScoredIdImpl sid2 = new ScoredIdImpl(1, 10);
        ScoredIdImpl sid3 = new ScoredIdImpl(1, 10, Collections.<SymbolValue<?>>emptyList());
        assertEquals(sid2, sid);
        assertEquals(sid3, sid);
        assertEquals(sid3, sid2);
    }
    
    @Test
    public void testGetId() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        assertEquals(1,sid.getId());
    }

    @Test
    public void testGetScore() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        assertEquals(10.5 , sid.getScore(), 0.0001);
    }

    @Test
    public void testHasChannel() {
        ArrayList<DoubleSymbolValue> channels = Lists.newArrayList(SymbolValue.of(fooSym, 1.0));
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, channels);
        channels.add(SymbolValue.of(barSym, 2.0));
        assertTrue(sid.hasUnboxedChannel(fooSym));
        assertFalse(sid.hasUnboxedChannel(barSym));
    }

    @Test
    public void testHasTypedChannel() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        assertTrue(sid.hasChannel(fooIntSym));
        assertFalse(sid.hasChannel(barStrSym));
    }

    @Test
    public void testChannel() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,Lists.newArrayList(SymbolValue.of(fooSym, 1.0)));
        assertEquals(1.0, sid.getUnboxedChannelValue(fooSym), 0.0001);
        try {
            sid.getUnboxedChannelValue(barSym);
            fail("expection expected");
        } catch (NullPointerException e) { /*expected */ }
    }

    @Test
    public void testTypedChannel() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        assertEquals(new Integer(1), sid.getChannelValue(fooIntSym));
        assertNull(sid.getChannelValue(barStrSym));
    }

    @Test
    public void testGetChannels() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,Lists.newArrayList(SymbolValue.of(fooSym, 1.0)));
        assertEquals(Collections.singleton(fooSym),sid.getUnboxedChannelSymbols());
    }
    
    @Test
    public void testGetTypedChannels() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        assertEquals(Collections.singleton(fooIntSym),sid.getChannelSymbols());
    }
    
    @Test
    public void testGetChannelsEmpty() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        assertEquals(Collections.emptySet(),sid.getUnboxedChannelSymbols());
        assertEquals(Collections.emptySet(),sid.getChannelSymbols());
    }

    @Test
    public void testEqualsAndHashCode() {
        List<SymbolValue<?>> channels = Lists.<SymbolValue<?>>newArrayList(
                SymbolValue.of(fooSym, 1.0),
                SymbolValue.of(fooIntSym, 1));
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, channels);
        ScoredIdImpl sid2 = new ScoredIdImpl(1, 10.5, channels);
        assertEquals(sid, sid2);
        assertEquals(sid.hashCode(), sid2.hashCode());
    }
    
    @Test
    public void testEqualsDifferentId() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        ScoredIdImpl sid2 = new ScoredIdImpl(2,10.5);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentScore() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.4);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentChannels() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooSym, 1.0)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(barSym, 1.0)));
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentTypedChannels() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(barStrSym, "hat")));
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentChannelValues() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooSym, 1.0)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooSym, 1.1)));
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentTypedChannelValues() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 2)));
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsChanneltoNoChannel() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooSym, 1.0)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsTypedChanneltoNoChannel() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, Lists.newArrayList(SymbolValue.of(fooIntSym, 1)));
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5);
        assertNotEquals(sid, sid2);
    }
}
