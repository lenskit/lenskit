/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

package org.grouplens.lenskit.util;

import org.grouplens.lenskit.util.Indexer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author Lingfei He
 *
 */
public class TestIndexer {
	Indexer ind;
	double[] values;
	MutableSparseVector vector;
	
	@Before
	public void creates(){
		ind = new Indexer();
		vector = new MutableSparseVector();
		values = new double[] {1,2,3};
	}
	
	@Test
	public void testConvertArrayToVector(){
		assertThat(ind.getObjectCount(), equalTo(0));
		ind.internId(0);
		ind.internId(1);
		ind.internId(2);
		assertThat(ind.getObjectCount(), equalTo(3));
		vector = ind.convertArrayToVector(values);
		assertThat(vector.get(0), equalTo(1.0));
		assertThat(vector.get(1), equalTo(2.0));
		assertThat(vector.get(2), equalTo(3.0));
	}

}
