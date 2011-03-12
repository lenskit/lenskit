/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens.knn.item;

import static org.junit.Assert.assertNotNull;

import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.RecommenderService;
import org.grouplens.reflens.baseline.UserMeanPredictor;
import org.grouplens.reflens.data.ExpensiveRatingDataTest;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;


/**
 * Test that the item-item recommender can be built against a real data set.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestItemItemRecommenderWithData extends ExpensiveRatingDataTest {
	private ItemRecommenderModule module;
	@Before
	public void createModule() {
		module = new ItemRecommenderModule();
	}
	
	@Test
	public void testItemItemBuild() {
		Injector inj = Guice.createInjector(module);
		RecommenderBuilder builder = inj.getInstance(RecommenderBuilder.class);
		RecommenderService rec = builder.build(dataSource);
		assertNotNull(rec);
		assertNotNull(rec.getRatingPredictor());
		assertNotNull(rec.getRatingRecommender());
	}
	
	@Test
	public void testItemItemWithBaseline() {
		module.setBaseline(UserMeanPredictor.class);
		Injector inj = Guice.createInjector(module);
		RecommenderBuilder builder = inj.getInstance(RecommenderBuilder.class);
		RecommenderService rec = builder.build(dataSource);
		assertNotNull(rec);
		assertNotNull(rec.getRatingPredictor());
		assertNotNull(rec.getRatingRecommender());
	}
}
