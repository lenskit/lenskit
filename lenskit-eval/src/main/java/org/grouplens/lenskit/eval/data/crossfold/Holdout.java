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

import java.util.List;

import org.grouplens.lenskit.data.event.Rating;

public class Holdout {
	private Order<Rating> order;
	private PartitionAlgorithm<Rating> partition;
	
	public Order<Rating> getOrder() {
    	return order;
    }
	public void setOrder(Order<Rating> order) {
    	this.order = order;
    }
	public PartitionAlgorithm<Rating> getPartition() {
    	return partition;
    }
	public void setPartition(PartitionAlgorithm<Rating> partition) {
    	this.partition = partition;
    }
	
	public int partition(List<Rating> ratings) {
		if (order == null || partition == null) {
			throw new IllegalStateException("Unconfigured holdout");
		}
		order.apply(ratings);
		return partition.partition(ratings);
	}
}
