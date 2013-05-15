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

public class TestImmutableTypedSideChannel {

    private final String a = "a";
    private final String b = "b";
    private final String c = "c";
    
    protected ImmutableTypedSideChannel<String> emptyDomainSideChannel() {
        return new ImmutableTypedSideChannel<String>(new long[]{});
    }
    
    protected ImmutableTypedSideChannel<String> emptySideChannel() {
        return new ImmutableTypedSideChannel<String>(new long[]{1,2,4});
    }
    
    protected ImmutableTypedSideChannel<String> simpleSideChannel() {
        long[] keys = {1,2,4};
        String[] values = {a,b,a};
        return new ImmutableTypedSideChannel<String>(keys,values);
    }
    
    protected ImmutableTypedSideChannel<String> singletonSideChannel() {
        long[] keys = {1};
        String[] values = {a};
        return new ImmutableTypedSideChannel<String>(keys,values);
    }
    
    @Test 
    public void testConstructors() {
        ImmutableTypedSideChannel<String> channel = new ImmutableTypedSideChannel<String>(new long[]{1,2});
        assertTrue(channel.isEmpty());
        
        channel = new ImmutableTypedSideChannel<String>(new long[]{1,2},new String[]{a,b});
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertEquals(b, channel.get(2));
        
        channel = new ImmutableTypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, 1);
        assertFalse(channel.isEmpty());
        assertEquals(a, channel.get(1));
        assertNull(channel.get(2));
        
        BitSet bs = new BitSet(2);
        bs.set(1);
        channel = new ImmutableTypedSideChannel<String>(new long[]{1,2},new String[]{a,b}, bs);
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        
        bs = new BitSet(3);
        bs.set(1);
        channel = new ImmutableTypedSideChannel<String>(new long[]{1,2,3},new String[]{a,b,c}, bs, 2);
        assertFalse(channel.isEmpty());
        assertEquals(b,channel.get(2));
        assertNull(channel.get(1));
        assertNull(channel.get(3));    
    }
    
    @Test
    public void testClear() {
        ImmutableTypedSideChannel<String> channel = simpleSideChannel();
        try {
            channel.clear();
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testSize() {
        assertEquals(0, emptySideChannel().size());
        assertEquals(1, singletonSideChannel().size());
        assertEquals(3, simpleSideChannel().size());
    }
    

    
    @Test
    public void testContains() {
        ImmutableTypedSideChannel<String> channel = simpleSideChannel();
        assertTrue(channel.containsKey(1));
        assertTrue(channel.containsKey(new Long(1)));
        assertFalse(channel.containsKey(3));
        assertFalse(channel.containsKey(new Long(3)));
        assertTrue(channel.containsValue(a));
        assertFalse(channel.containsValue(c));
    }
    
    @Test
    public void testDefaultReturnValue(){
        ImmutableTypedSideChannel<String> channel = emptySideChannel();
        assertNull(channel.defaultReturnValue());
        assertNull(channel.get(1));
        try {
            channel.defaultReturnValue(a);
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testPut() {
        ImmutableTypedSideChannel<String> channel = emptySideChannel();
        try {
            channel.put(1,a);
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testRemove() {
        ImmutableTypedSideChannel<String> channel = simpleSideChannel();
        try {
            channel.remove(1);
            fail("imutable side channel should throw an exception on modifiers.");
        } catch (UnsupportedOperationException e) {/* expected */}
    }
    
    @Test
    public void testKeySet() {
        assertTrue(emptySideChannel().keySet().isEmpty());
        assertTrue(emptyDomainSideChannel().keySet().isEmpty());
        LongSet expected = new LongArraySet(new long[]{1,2,4}); 
        assertEquals(expected, simpleSideChannel().keySet());
    }
    
    @Test
    public void testMutableCopy() {
        ImmutableTypedSideChannel<String> simple = simpleSideChannel();
        TypedSideChannel<String> mutCopy = simple.mutableCopy();
        assertFalse(mutCopy.containsKey(3));
        assertEquals(a,mutCopy.get(1));
        assertEquals(b,mutCopy.get(2));
        assertEquals(a,mutCopy.get(4));

        //copy doesn't effect simple.
        mutCopy.remove(4);
        mutCopy.put(1, c);
        assertEquals(a,simple.get(4));
        assertEquals(a,simple.get(1));
    }
    
    @Test
    public void testImmutableCopy() {
        ImmutableTypedSideChannel<String> simple = simpleSideChannel();
        assertEquals(simple, simple.immutableCopy());
        assertEquals(simple, simple.freeze());
    }
}
