package org.grouplens.reflens;

import java.net.URL;

import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SimpleFileDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class providing access to the MovieLens rating data for expensive tests.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class ExpensiveRatingDataTest {
	public static final String DATA_PATH = "org/grouplens/movielens/mldata/ml100k/ratings.dat";
	protected RatingDataSource dataSource;
	
	@BeforeClass
	public static void printMessage() {
		System.out.println("This test uses the MovieLens 100K data set.");
		System.out.println("This data set is only licensed for non-commercial use.");
		System.out.println("For more information, visit http://reflens.grouplens.org/ml-data/");
	}

	@BeforeClass
	public static void getDataURL() {
		
	}

	public ExpensiveRatingDataTest() {
		super();
	}

	@Before
	public void createDataSource() {
		URL dataUrl = ClassLoader.getSystemClassLoader().getResource(DATA_PATH);
		dataSource = new SimpleFileDataSource(dataUrl);
	}

	@After
	public void closeDataSource() {
		dataSource.close();
		dataSource = null;
	}

}