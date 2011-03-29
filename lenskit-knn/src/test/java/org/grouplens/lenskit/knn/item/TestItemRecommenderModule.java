/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.item;

import static org.grouplens.common.test.Matchers.isAssignableTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.params.ItemSimilarity;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.SimilarityDamper;
import org.grouplens.lenskit.params.meta.Parameters;
import org.grouplens.lenskit.testing.RecommenderModuleTest;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestItemRecommenderModule extends RecommenderModuleTest {
    private static final double EPSILON = 1.0e-6;
    private ItemItemCFModule module;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        module = new ItemItemCFModule();
    }

    public Module getModule() {
        return module;
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#getNeighborhoodSize()}.
     */
    @Test
    public void testGetNeighborhoodSize() {
        assertEquals(Parameters.getDefaultInt(NeighborhoodSize.class), module.knn.getNeighborhoodSize());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#setNeighborhoodSize(int)}.
     */
    @Test
    public void testSetNeighborhoodSize() {
        module.knn.setNeighborhoodSize(40);
        assertEquals(40, module.knn.getNeighborhoodSize());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#getSimilarityDamping()}.
     */
    @Test
    public void testGetSimilarityDamping() {
        assertEquals(Parameters.getDefaultDouble(SimilarityDamper.class), module.knn.getSimilarityDamping(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#setSimilarityDamping(double)}.
     */
    @Test
    public void testSetSimilarityDamping() {
        module.knn.setSimilarityDamping(5);
        assertEquals(5, module.knn.getSimilarityDamping(), EPSILON);
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#getItemSimilarity()}.
     */
    @Test
    public void testGetItemSimilarity() {
        assertThat(module.knn.getItemSimilarity(), isAssignableTo(Parameters.getDefaultClass(ItemSimilarity.class)));
    }

    /**
     * Test method for {@link org.grouplens.lenskit.knn.item.ItemItemCFModule#setItemSimilarity(java.lang.Class)}.
     */
    @Test
    public void testSetItemSimilarity() {
        module.knn.setItemSimilarity(DummySimilarity.class);
        assertThat(module.knn.getItemSimilarity(), isAssignableTo(DummySimilarity.class));
    }

    @Test
    public void testInjectSimilarity() {
        module.knn.setItemSimilarity(DummySimilarity.class);
        module.knn.setSimilarityDamping(39.8);
        Similarity<? super MutableSparseVector> sim;
        sim = inject(Key.get(new TypeLiteral<Similarity<? super SparseVector>>(){}, ItemSimilarity.class));
        assertThat(sim, instanceOf(DummySimilarity.class));
        DummySimilarity dsim = (DummySimilarity) sim;
        assertEquals(39.8, dsim.damping, EPSILON);
    }

    private static class DummySimilarity implements Similarity<SparseVector> {
        public final double damping;

        @SuppressWarnings("unused")
        @Inject
        public DummySimilarity(@SimilarityDamper double d) {
            damping = d;
        }

        @Override
        public double similarity(SparseVector vec1, SparseVector vec2) {
            return 0;
        }

    }
}
