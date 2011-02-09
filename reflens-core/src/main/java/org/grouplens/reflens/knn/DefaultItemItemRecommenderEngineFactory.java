package org.grouplens.reflens.knn;

/**
 * Factory creating {@link ItemItemRecommenderEngine}s directly.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class DefaultItemItemRecommenderEngineFactory implements
		ItemItemRecommenderEngineFactory {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.knn.ItemItemRecommenderEngineFactory#create(org.grouplens.reflens.knn.ItemItemModel)
	 */
	@Override
	public ItemItemRecommenderEngine create(ItemItemModel model) {
		return new ItemItemRecommenderEngine(model);
	}

}
