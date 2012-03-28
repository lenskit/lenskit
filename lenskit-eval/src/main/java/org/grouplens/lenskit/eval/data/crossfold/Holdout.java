/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.util.List;
import java.util.Random;

import org.grouplens.lenskit.data.event.Rating;

/**
 * A train-test holdout method.
 */
public class Holdout {
	private final Order<Rating> order;
	private final PartitionAlgorithm<Rating> partitionMethod;

    public Holdout(Order<Rating> ord, PartitionAlgorithm<Rating> part) {
        order = ord;
        partitionMethod = part;
    }
	
	public Order<Rating> getOrder() {
    	return order;
    }
	public PartitionAlgorithm<Rating> getPartitionMethod() {
    	return partitionMethod;
    }

	public int partition(List<Rating> ratings, Random rng) {
		if (order == null || partitionMethod == null) {
			throw new IllegalStateException("Unconfigured holdout");
		}
		order.apply(ratings, rng);
		return partitionMethod.partition(ratings);
	}
}
