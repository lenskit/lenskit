/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
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

package org.grouplens.reflens.item;

import java.util.Collection;

import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.data.RatingVector;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ItemItemRecommenderFactory<U,I> implements RecommenderFactory<U, I> {
	Provider<ItemItemRecommender<U,I>> recProvider;
	
	@Inject
	ItemItemRecommenderFactory(Provider<ItemItemRecommender<U,I>> recProvider) {
		this.recProvider = recProvider;
	}
	
	@Override
	public ItemItemRecommender<U,I> build(Collection<RatingVector<U,I>> data) {
		ItemItemRecommender<U,I> rec = recProvider.get();
		rec.buildModel(data);
		return rec;
	}
}
