package org.grouplens.reflens.knn.item;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.RecommenderService;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SimpleFileDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * Test that the item-item recommender can be built against a real data set.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestItemItemRecommenderWithData {
	private static URL dataUrl;
	private RatingDataSource dataSource;
	private Module module;
	
	@BeforeClass
	public static void printMessage() {
		System.out.println("This test uses the MovieLens 100K data set.");
		System.out.println("This data set is only licensed for non-commercial use.");
		System.out.println("For more information, visit http://reflens.grouplens.org/ml-data/");
	}
	
	@BeforeClass
	public static void getDataURL() {
		dataUrl = ClassLoader.getSystemClassLoader().getResource("org/grouplens/movielens/mldata/ml100k/ratings.dat");
	}
	
	@Before
	public void createDataSource() {
		dataSource = new SimpleFileDataSource(dataUrl);
	}
	
	@Before
	public void createModel() {
		module = new ItemRecommenderModule();
	}
	
	@After
	public void closeDataSource() {
		dataSource.close();
		dataSource = null;
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
}
