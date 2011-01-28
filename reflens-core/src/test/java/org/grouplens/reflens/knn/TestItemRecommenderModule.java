package org.grouplens.reflens.knn;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.grouplens.common.test.Matchers.*;
import static org.grouplens.common.test.GuiceHelpers.inject;

import org.grouplens.reflens.TestRecommenderModule;
import org.grouplens.reflens.data.RatingVector;
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
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#getNeighborhoodSize()}.
	 */
	@Test
	public void testGetNeighborhoodSize() {
		assertEquals(NeighborhoodSize.DEFAULT_VALUE, module.getNeighborhoodSize());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#setNeighborhoodSize(int)}.
	 */
	@Test
	public void testSetNeighborhoodSize() {
		module.setNeighborhoodSize(40);
		assertEquals(40, module.getNeighborhoodSize());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#getSimilarityDamping()}.
	 */
	@Test
	public void testGetSimilarityDamping() {
		assertEquals(SimilarityDamper.DEFAULT_VALUE, module.getSimilarityDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#setSimilarityDamping(double)}.
	 */
	@Test
	public void testSetSimilarityDamping() {
		module.setSimilarityDamping(5);
		assertEquals(5, module.getSimilarityDamping(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#getItemSimilarity()}.
	 */
	@Test
	public void testGetItemSimilarity() {
		assertThat(module.getItemSimilarity(), isAssignableTo(ItemSimilarity.DEFAULT_VALUE));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.knn.ItemRecommenderModule#setItemSimilarity(java.lang.Class)}.
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
