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
