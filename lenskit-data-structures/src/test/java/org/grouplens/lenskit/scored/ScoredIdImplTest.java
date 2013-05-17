package org.grouplens.lenskit.scored;

import static org.junit.Assert.*;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;

import java.util.Collections;
import java.util.HashMap;

import org.grouplens.lenskit.symbols.Symbol;
import org.junit.Test;

public class ScoredIdImplTest {

    private final Symbol fooSym = Symbol.of("foo");
    private final Symbol barSym = Symbol.of("bar");
    
    @Test
    public void testConstructors() {
        ScoredIdImpl sid = new ScoredIdImpl(1, 10);
        ScoredIdImpl sid2 = new ScoredIdImpl(1, 10, null);
        ScoredIdImpl sid3 = new ScoredIdImpl(1, 10, new Reference2DoubleArrayMap<Symbol>());
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
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels);
        channels.put(barSym,2.0); // shouldn't effect sid.
        assertTrue(sid.hasChannel(fooSym));
        assertFalse(sid.hasChannel(barSym));
    }

    @Test
    public void testChannel() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels);
        assertEquals(1.0, sid.channel(fooSym), 0.0001);
        try {
            sid.channel(barSym);
            fail("expection expected");
        } catch (IllegalArgumentException e) { /*expected */ }
    }

    @Test
    public void testGetChannels() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5,channels);
        assertEquals(Collections.singleton(fooSym),sid.getChannels());
    }
    
    @Test
    public void testGetChannelsEmpty() {
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5);
        assertEquals(Collections.emptySet(),sid.getChannels());
    }

    @Test
    public void testEqualsAndHashCode() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
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
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, channels);
        Reference2DoubleMap<Symbol> channels2 = new Reference2DoubleArrayMap<Symbol>();
        channels2.put(barSym,1.0);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, channels2);
        assertNotEquals(sid, sid2);
    }
    
    @Test
    public void testEqualsDifferentChannelValues() {
        Reference2DoubleMap<Symbol> channels = new Reference2DoubleArrayMap<Symbol>();
        channels.put(fooSym,1.0);
        ScoredIdImpl sid = new ScoredIdImpl(1,10.5, channels);
        Reference2DoubleMap<Symbol> channels2 = new Reference2DoubleArrayMap<Symbol>();
        channels2.put(fooSym,1.1);
        ScoredIdImpl sid2 = new ScoredIdImpl(1,10.5, channels2);
        assertNotEquals(sid, sid2);
    }
}
