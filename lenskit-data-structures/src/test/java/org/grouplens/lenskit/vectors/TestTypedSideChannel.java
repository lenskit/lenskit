package org.grouplens.lenskit.vectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.BitSet;

import org.junit.Test;

public class TestTypedSideChannel {

    private final String a = "a";
    private final String b = "b";
    private final String c = "c";
    
    protected TypedSideChannel<String> emptyDomainSideChannel() {
        return new TypedSideChannel<String>(new long[]{});
    }
    
    protected TypedSideChannel<String> emptySideChannel() {
        return new TypedSideChannel<String>(new long[]{1,2,4});
    }
    
    protected TypedSideChannel<String> simpleSideChannel() {
        long[] keys = {1,2,4};
        String[] values = {a,b,a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    protected TypedSideChannel<String> singletonSideChannel() {
        long[] keys = {1};
        String[] values = {a};
        return new TypedSideChannel<String>(keys,values);
    }
    
    @Test 
    public void testConstructors() {
        TypedSideChannel<String> channel = new TypedSideChannel<String>(new long[]{1,2});
        assertTrue(channel.isEmpty());
        
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b});
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertEquals(b, channel.get(2));
        
        channel = new TypedSideChannel<String>(new long[]{1,2},1);
        assertTrue(channel.isEmpty());
        channel.put(1, a); //check if this is in domain
        try {
            channel.put(2, b);
            fail("2 shouldn't be in the domain of channel.");
        } catch (IllegalArgumentException e) {/* ignore */}
        
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, 1);
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertNull(channel.get(2));
        
        BitSet bs = new BitSet(2);
        bs.set(1);
        channel = new TypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, bs);
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        channel.put(1, a); //check if this is in domain.
        
        bs = new BitSet(3);
        bs.set(1);
        channel = new TypedSideChannel<String>(new long[]{1,2,3},new String[]{a,b,c}, bs, 2);
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        assertNull(channel.get(3));
        channel.put(1, a); //check if this is in domain.
        try {
            channel.put(3, c);
            fail("3 shouldn't be in the domain of channel.");
        } catch (IllegalArgumentException e) {/* ignore */}
        
    }
    
    @Test
    public void testClear() {
        TypedSideChannel<String> channel = simpleSideChannel();
        assertFalse(channel.isEmpty());
        channel.clear();
        assertTrue(channel.isEmpty());
    }
    
    @Test
    public void testSize() {
        assertEquals(0, emptySideChannel().size());
        assertEquals(1, singletonSideChannel().size());
        assertEquals(3, simpleSideChannel().size());
    }
    

    
    @Test
    public void testContains() {
        TypedSideChannel<String> channel = simpleSideChannel();
        assertTrue(channel.containsKey(1));
        assertTrue(channel.containsKey(new Long(1)));
        assertFalse(channel.containsKey(3));
        assertFalse(channel.containsKey(new Long(3)));
        assertTrue(channel.containsValue(a));
        assertFalse(channel.containsValue(c));
    }
    
    @Test
    public void testDefaultReturnValue(){
        TypedSideChannel<String> channel = emptySideChannel();
        assertNull(channel.defaultReturnValue());
        assertNull(channel.get(1));
        channel.defaultReturnValue(a);
        assertEquals(a, channel.defaultReturnValue());
        assertEquals(a, channel.get(1));
    }
    
    @Test
    public void testPut() {
        TypedSideChannel<String> channel = emptySideChannel();
        channel.put(1,a);
        channel.put(new Long(2), b);
        assertEquals(a,channel.get(new Long(1)));
        assertEquals(b,channel.get(2));
    }
    
    @Test
    public void testRemove() {
        TypedSideChannel<String> channel = emptySideChannel();
        channel.put(1,a);
        assertEquals(a,channel.get(1));
        channel.remove(1);
        assertNull(channel.get(1));
        
        channel.put(1,a);
        assertEquals(a,channel.get(new Long(1)));
        channel.remove(1);
        assertNull(channel.get(1));
    }
    
    @Test
    public void testKeySet() {
        assertTrue(emptySideChannel().keySet().isEmpty());
        assertTrue(emptyDomainSideChannel().keySet().isEmpty());
        LongSet expected = new LongArraySet(new long[]{1,2,4}); 
        assertEquals(expected, simpleSideChannel().keySet());
    }
}
