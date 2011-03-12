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
package org.grouplens.reflens.testing;

import java.net.URL;

import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SimpleFileDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class providing access to the MovieLens rating data for expensive tests.
 * 
 * <p>This class provides the machinery to access the MovieLens 100K rating data
 * for expensive data-based tests.  It's used by the extra data tests in RefLens,
 * and can be used to implement your own data-based tests (subject to the licensing
 * terms of the MovieLens rating data).</p>
 * 
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