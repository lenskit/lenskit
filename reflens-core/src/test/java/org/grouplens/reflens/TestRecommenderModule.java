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
package org.grouplens.reflens;

import static org.grouplens.common.test.Matchers.isAssignableTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import static org.grouplens.common.test.GuiceHelpers.*;

import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.params.MaxRating;
import org.grouplens.reflens.params.MeanDamping;
import org.grouplens.reflens.params.MinRating;
import org.grouplens.reflens.params.ThreadCount;
import org.grouplens.reflens.params.meta.Parameters;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestRecommenderModule {
	public static final double EPSILON = 1.0e-6;
	private RecommenderModule module;
	
	@Before
	public void setUp() {
		module = new RecommenderModule();
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		module.setName("foo");
		assertEquals("foo", module.getName());
	}
	
	@Test
	public void testDefaults() {
		assertEquals(Parameters.getDefaultDouble(MeanDamping.class), module.getDamping(), EPSILON);
		assertEquals(Parameters.getDefaultDouble(MinRating.class), module.getMinRating(), EPSILON);
		assertEquals(Parameters.getDefaultDouble(MaxRating.class), module.getMaxRating(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#getThreadCount()}.
	 */
	@Test
	public void testGetThreadCount() {
		assertEquals(Runtime.getRuntime().availableProcessors(), module.getThreadCount());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setThreadCount(int)}.
	 */
	@Test
	public void testSetThreadCount() {
		// 57 is a safe processor count to make sure the set method changes it
		// unless you are on a very strange machine
		module.setThreadCount(57);
		assertEquals(57, module.getThreadCount());
	}
	
	@Test
	public void testInjectThreadCount() {
		int nthreads = inject(module, int.class, ThreadCount.class);
		assertEquals(Runtime.getRuntime().availableProcessors(), nthreads);
		module.setThreadCount(57);
		nthreads = inject(module, int.class, ThreadCount.class);
		assertEquals(57, nthreads);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setDamping(double)}.
	 */
	@Test
	public void testSetDamping() {
		module.setDamping(500);
		assertEquals(500, module.getDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setMinRating(double)}.
	 */
	@Test
	public void testSetMinRating() {
		module.setMinRating(-5);
		assertEquals(-5, module.getMinRating(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setMaxRating(double)}.
	 */
	@Test
	public void testSetMaxRating() {
		module.setMaxRating(42);
		assertEquals(42, module.getMaxRating(), EPSILON);
	}
	
	public void testInjectRatingRange() {
		double min = inject(module, double.class, MinRating.class);
		double max = inject(module, double.class, MaxRating.class);
		assertEquals(1, min, EPSILON);
		assertEquals(5, max, EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#getBaseline()}.
	 */
	@Test
	public void testGetBaseline() {
		assertNull(module.getBaseline());
		assertNull(inject(module, RatingPredictorBuilder.class, BaselinePredictor.class));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.RecommenderModule#setBaseline(java.lang.Class)}.
	 */
	@Test
	public void testSetBaselineClass() {
		module.setBaseline(DudBaseline.class);
		assertThat(module.getBaseline(), isAssignableTo(DudBaseline.class));
		RatingPredictorBuilder bldr =
			inject(module, RatingPredictorBuilder.class, BaselinePredictor.class);
		assertThat(bldr, instanceOf(DudBaseline.class));
	}
	
	private static class DudBaseline implements RatingPredictorBuilder {
		@Override public RatingPredictor build(RatingDataSource data) {
			throw new UnsupportedOperationException();
		}
	}
	
	@Test
	public void testGetConstantBaselineValue() {
		assertEquals(ConstantPredictor.Value.DEFAULT_VALUE, module.getConstantBaselineValue(), EPSILON);
	}
	
	@Test
	public void testSetConstantBaselineValue() {
		module.setConstantBaselineValue(10);
		assertEquals(10, module.getConstantBaselineValue(), EPSILON);
	}
	
	@Test
	public void testInjectConstantBaseline() {
		module.setConstantBaselineValue(3);
		module.setBaseline(ConstantPredictor.class);
		RatingPredictorBuilder pred = inject(module, RatingPredictorBuilder.class, BaselinePredictor.class);
		assertThat(pred, instanceOf(ConstantPredictor.Builder.class));
		ConstantPredictor.Builder b = (ConstantPredictor.Builder) pred;
		assertEquals(3, b.getValue(), EPSILON);
	}
}
