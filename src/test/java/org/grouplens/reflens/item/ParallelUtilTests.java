/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 <AUTHOR> (TODO: insert author name)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.item;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.grouplens.reflens.item.ParallelItemItemRecommenderBuilder.SymmetricRowCounter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ParallelUtilTests {
	private SymmetricRowCounter counter;
	
	@Before
	public void makeCounter() {
		counter = new SymmetricRowCounter();
	}
	
	@Test
	public void initial() {
		assertEquals(0, counter.getRow());
		assertEquals(0, counter.getColumn());
	}
	
	@Test
	public void advanceZero() {
		counter.advance(0);
		assertEquals(0, counter.getRow());
		assertEquals(0, counter.getColumn());
	}
	
	@Test
	public void advanceOne() {
		counter.advance(1);
		assertEquals(1, counter.getRow());
		assertEquals(0, counter.getColumn());
	}
	
	@Test
	public void advanceTwice() {
		counter.advance(1);
		counter.advance(2);
		assertEquals(1, counter.getRow());
		assertEquals(1, counter.getColumn());
	}
	
	@Test
	public void advanceTwo() {
		counter.advance(2);
		assertEquals(1, counter.getRow());
		assertEquals(1, counter.getColumn());
	}
	
	@Test
	public void advanceThree() {
		counter.advance(3);
		assertEquals(2, counter.getRow());
		assertEquals(0, counter.getColumn());
	}
	
	@Test
	public void randomMany() {
		Random rand = new Random();
		int job = 0;
		for (int row = 0; row < 1000; row++) {
			for (int col = 0; col <= row; col++) {
				if (rand.nextBoolean()) {
					counter.advance(job);
					assertEquals(row, counter.getRow());
					assertEquals(col, counter.getColumn());
				}
				job++;
			}
		}
	}
}
