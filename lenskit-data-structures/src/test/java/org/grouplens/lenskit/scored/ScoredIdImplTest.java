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
package org.grouplens.lenskit.scored;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;

import java.util.Collections;

import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.junit.Test;

public class ScoredIdImplTest {

    private final Symbol fooSym = Symbol.of("foo");
    private final Symbol barSym = Symbol.of("bar");
    private final TypedSymbol<Integer> fooIntSym = TypedSymbol.of("foo", Integer.class);
    private final TypedSymbol<String> barStrSym = TypedSymbol.of("bar", String.class);
    
    
    @Test
    public void testConstructors() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10);
        ScoredIdImpl sid2 = new ScoredIdImpl(1, 10, null, null);
        ScoredIdImpl sid3 = new ScoredIdImpl(1, 10, new Reference2DoubleArrayMap<Symbol>(), null);
        ScoredIdImpl sid4 = new ScoredIdImpl(1, 10, new Reference2DoubleArrayMap<Symbol>(), 
                                             new Reference2ObjectArrayMap<TypedSymbol<?>,Object>());
        assertEquals(sid2, sid);
        assertEquals(sid3, sid);
        assertEquals(sid4, sid);
        assertEquals(sid3, sid2);
        assertEquals(sid4, sid2);
        assertEquals(sid4, sid3);
        
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
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels,null);
        channels.put(barSym,2.0); // shouldn't effect sid.
        assertTrue(sid.hasChannel(fooSym));
        assertFalse(sid.hasChannel(barSym));
    }

    @Test
    public void testHasTypedChannel() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, null, typedChannels);
        typedChannels.put(barStrSym,"hat"); // shouldn't effect sid.
        assertTrue(sid.hasChannel(fooIntSym));
        assertFalse(sid.hasChannel(barStrSym));
    }

    @Test
    public void testChannel() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels,null);
        assertEquals(1.0, sid.channel(fooSym), 0.0001);
        try {
            sid.channel(barSym);
            fail("expection expected");
        } catch (IllegalArgumentException e) { /*expected */ }
    }

    @Test
    public void testTypedChannel() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, null, typedChannels);
        assertEquals(new Integer(1), sid.channel(fooIntSym));
        try {
            sid.channel(barStrSym);
            fail("expection expected");
        } catch (IllegalArgumentException e) { /*expected */ }
    }

    @Test
    public void testGetChannels() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels,null);
        assertEquals(Collections.singleton(fooSym),sid.getChannels());
    }
    
    @Test
    public void testGetTypedChannels() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, null, typedChannels);
        assertEquals(Collections.singleton(fooIntSym),sid.getTypedChannels());
    }
    
    @Test
    public void testGetChannelsEmpty() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        assertEquals(Collections.emptySet(),sid.getChannels());
        assertEquals(Collections.emptySet(),sid.getTypedChannels());
    }

    @Test
    public void testEqualsAndHashCode() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1, 10.5, channels, typedChannels);
        ScoredIdImpl sid2 = new ScoredIdImpl(1, 10.5, channels, typedChannels);
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
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, channels, null);
        Reference2DoubleMap<Symbol> channels2 = new Reference2DoubleArrayMap<Symbol>();
        channels2.put(barSym,1.0);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, channels2, null);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentTypedChannels() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, null, typedChannels);
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels2 = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(barStrSym,"hat");
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, null, typedChannels2);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentChannelValues() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, channels, null);
        Reference2DoubleMap<Symbol> channels2 = new Reference2DoubleArrayMap<Symbol>();
        channels2.put(fooSym,1.1);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, channels2, null);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentTypedChannelValues() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, null, typedChannels);
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels2 = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,2);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, null, typedChannels2);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsChanneltoNoChannel() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, channels, null);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, null, null);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsTypedChanneltoNoChannel() {
        Reference2ObjectMap<TypedSymbol<?>,Object> typedChannels = new Reference2ObjectArrayMap<TypedSymbol<?>, Object>();
        typedChannels.put(fooIntSym,1);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, null, typedChannels);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, null, null);
        assertNotEquals(sid, sid2);
    }
}
