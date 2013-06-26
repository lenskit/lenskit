package org.grouplens.lenskit.collections;

import org.junit.Test;

import java.util.BitSet;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BitSetPointerTest {
    @Test
    public void testEmptyBitSet() {
        BitSetPointer bsp = new BitSetPointer(new BitSet());
        assertThat(bsp.isAtEnd(), equalTo(true));
        assertThat(bsp.advance(), equalTo(false));
        try {
            bsp.getInt();
            fail("getInt should fail on empty iterator");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testSimpleBitSet() {
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(1);
        bs.set(3);
        BitSetPointer bsp = new BitSetPointer(bs);
        assertThat(bsp.getInt(), equalTo(0));
        assertThat(bsp.isAtEnd(), equalTo(false));
        assertThat(bsp.advance(), equalTo(true));
        assertThat(bsp.getInt(), equalTo(1));
        assertThat(bsp.advance(), equalTo(true));
        assertThat(bsp.isAtEnd(), equalTo(false));
        assertThat(bsp.getInt(), equalTo(3));
        assertThat(bsp.advance(), equalTo(false));
        assertThat(bsp.isAtEnd(), equalTo(true));
    }

    @Test
    public void testStart() {
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(2);
        bs.set(3);
        BitSetPointer bsp = new BitSetPointer(bs, 1);
        assertThat(bsp.getInt(), equalTo(2));
        assertThat(bsp.isAtEnd(), equalTo(false));
        assertThat(bsp.advance(), equalTo(true));
        assertThat(bsp.getInt(), equalTo(3));
        assertThat(bsp.advance(), equalTo(false));
        assertThat(bsp.isAtEnd(), equalTo(true));
    }

    @Test
    public void testLimit() {
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(2);
        bs.set(3);
        bs.set(5);
        BitSetPointer bsp = new BitSetPointer(bs, 0, 5);
        assertThat(bsp.getInt(), equalTo(0));
        assertThat(bsp.isAtEnd(), equalTo(false));
        assertThat(bsp.advance(), equalTo(true));
        assertThat(bsp.getInt(), equalTo(2));
        assertThat(bsp.isAtEnd(), equalTo(false));
        assertThat(bsp.advance(), equalTo(true));
        assertThat(bsp.getInt(), equalTo(3));
        assertThat(bsp.advance(), equalTo(false));
        assertThat(bsp.isAtEnd(), equalTo(true));
    }
}
