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
package org.grouplens.lenskit.eval.data.crossfold;

import static java.lang.Math.max;

import java.util.List;

/**
 * Partition a list by holding out a fixed number of elements.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <E>
 */
public class CountPartition<E> implements PartitionAlgorithm<E> {
	
	private int count;

	/**
	 * Create a count partitioner.
	 * @param n The number of items to put in the second partition.
	 */
	public CountPartition(int n) {
		count = n;
	}

	@Override
	public int partition(List<E> data) {
		return max(0, data.size() - count);
	}

}
