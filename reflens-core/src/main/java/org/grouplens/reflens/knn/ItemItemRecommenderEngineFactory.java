package org.grouplens.reflens.knn;

import javax.annotation.Nonnull;

import com.google.inject.ImplementedBy;

/**
 * Factory for creating new item-item recommender engines from models.
 * 
 * Implementations of this interface take an {@link ItemItemModel} and create
 * an {@link ItemItemRecommenderEngine} backed by it.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ImplementedBy(DefaultItemItemRecommenderEngineFactory.class)
public interface ItemItemRecommenderEngineFactory {
	/**
	 * Create a new recommender engine.
	 * @param model The model backing the engine.
	 * @return The newly-constructed recommender engine.
	 */
	@Nonnull
	ItemItemRecommenderEngine create(@Nonnull ItemItemModel model);
}
