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
package org.grouplens.reflens.knn.item;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.grouplens.common.test.Matchers.*;
import static org.grouplens.common.test.GuiceHelpers.inject;

import org.grouplens.reflens.TestRecommenderModule;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.knn.Similarity;
import org.grouplens.reflens.knn.item.ItemRecommenderModule;
import org.grouplens.reflens.knn.params.ItemSimilarity;
import org.grouplens.reflens.knn.params.NeighborhoodSize;
import org.grouplens.reflens.knn.params.SimilarityDamper;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestItemRecommenderModule {
	private static final double EPSILON = TestRecommenderModule.EPSILON;
	private ItemRecommenderModule module;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		module = new ItemRecommenderModule();
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#getNeighborhoodSize()}.
	 */
	@Test
	public void testGetNeighborhoodSize() {
		assertEquals(NeighborhoodSize.DEFAULT_VALUE, module.getNeighborhoodSize());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#setNeighborhoodSize(int)}.
	 */
	@Test
	public void testSetNeighborhoodSize() {
		module.setNeighborhoodSize(40);
		assertEquals(40, module.getNeighborhoodSize());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#getSimilarityDamping()}.
	 */
	@Test
	public void testGetSimilarityDamping() {
		assertEquals(SimilarityDamper.DEFAULT_VALUE, module.getSimilarityDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#setSimilarityDamping(double)}.
	 */
	@Test
	public void testSetSimilarityDamping() {
		module.setSimilarityDamping(5);
		assertEquals(5, module.getSimilarityDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#getItemSimilarity()}.
	 */
	@Test
	public void testGetItemSimilarity() {
		assertThat(module.getItemSimilarity(), isAssignableTo(ItemSimilarity.DEFAULT_VALUE));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.item.ItemRecommenderModule#setItemSimilarity(java.lang.Class)}.
	 */
	@Test
	public void testSetItemSimilarity() {
		module.setItemSimilarity(DummySimilarity.class);
		assertThat(module.getItemSimilarity(), isAssignableTo(DummySimilarity.class));
	}
	
	@Test
	public void testInjectSimilarity() {
		module.setItemSimilarity(DummySimilarity.class);
		module.setSimilarityDamping(39.8);
		Similarity<? super RatingVector> sim;
		sim = inject(module, new TypeLiteral<Similarity<? super RatingVector>>(){}, ItemSimilarity.class);
		assertThat(sim, instanceOf(DummySimilarity.class));
		DummySimilarity dsim = (DummySimilarity) sim;
		assertEquals(39.8, dsim.damping, EPSILON);
	}
	
	private static class DummySimilarity implements Similarity<RatingVector> {
		public final double damping;
		
		@SuppressWarnings("unused")
		@Inject
		public DummySimilarity(@SimilarityDamper double d) {
			damping = d;
		}

		@Override
		public double similarity(RatingVector vec1, RatingVector vec2) {
			return 0;
		}
		
	}
}
