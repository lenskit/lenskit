/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
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

/**
 * 
 */
package org.grouplens.reflens.baseline;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;

import java.util.Collections;
import java.util.List;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingCollectionDataSource;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.vector.MutableSparseVector;
import org.grouplens.reflens.data.vector.SparseVector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestConstantPredictor {
	
	private ConstantPredictor.Builder builder;

	@Before
	public void setUp() {
		builder = new ConstantPredictor.Builder(5);
	}
	
	@Test
	public void testValue() {
		assertEquals(5, builder.getValue(), 1.0e-6); 
	}
	
	@Test
	public void testNullBuild() {
		RatingPredictor pred = builder.build(null);
		SparseVector map = new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(5, score.getScore(), 0.00001);
	}
	
	@Test
	public void testDataSourceBuild() {
		List<Rating> ratings = Collections.emptyList();
		SparseVector map = new MutableSparseVector(Long2DoubleMaps.EMPTY_MAP);
		RatingDataSource source = new RatingCollectionDataSource(ratings);
		RatingPredictor pred = builder.build(source);
		ScoredId score = pred.predict(10l, map, 2l);
		assertEquals(5, score.getScore(), 0.00001);
	}
}
