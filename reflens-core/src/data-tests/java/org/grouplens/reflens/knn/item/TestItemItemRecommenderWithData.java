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
