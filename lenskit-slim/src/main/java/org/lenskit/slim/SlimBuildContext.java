package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.api.Result;
import org.lenskit.inject.Shareable;
import org.lenskit.results.ResultAccumulator;
import org.lenskit.util.collections.LongUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(SimpleItemItemModelProvider.class)
public class SimpleItemItemModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2DoubleMap itemMeans;
    private final Map<Long,Long2DoubleMap> innerproducts;


    /**
     * Create a item-item model
     * @param means item ratings mean
     * @param dotproducts a mapping of item id to inner-products of each two items
     */
    public SimpleItemItemModel(Long2DoubleMap means, Map<Long,Long2DoubleMap> dotproducts) {
        itemMeans = LongUtils.frozenMap(means);
        innerproducts = dotproducts;
    }

    /**
     * Get the vector of item mean ratings.
     * @return The vector of item mean ratings.
     */
    public Long2DoubleMap getItemMeans() {
        return itemMeans;
    }

    /**
     * Get the neighbors of an item.
     * @return The neighbors of the item, sorted by decreasing score.
     */
    public Long2DoubleMap getNeighbors(long item) {
        Long2DoubleMap nbrs = neighborhoods.get(item);
        if (nbrs == null) {
            return new Long2DoubleOpenHashMap();
        } else {
            return nbrs;
        }
    }

    /**
     * Get all neighborhoods inner-products with the given item id
     * @param itemId reference item id
     * @return a mapping of neighborhoods' id to inner-products
     */
    public Long2DoubleMap getNeighborsInnerProduct(long itemId) {
        Long2DoubleMap candidates = innerproducts.get(itemId);
        if (candidates == null) {
            return new Long2DoubleOpenHashMap();
        } else {
            Long2DoubleMap nbrs = new Long2DoubleOpenHashMap(candidates);
            nbrs.remove(itemId);
            return nbrs;
        }
    }

    public Map<Long,Long2DoubleMap> getInnerproducts() { return innerproducts;}

    /**
     * Get n neighborhoods inner-products with the given item id
     * @param itemId reference item id
     * @param n number of neighborhoods
     * @return a mapping of neighborhoods' id to inner-products with the reference item id
     */
    public Long2DoubleMap getNeighborsInnerProduct(long itemId, int n) {
        List<Result> results;
        ResultAccumulator accum = ResultAccumulator.create(n);
        Long2DoubleMap candidates = neighborhoods.get(itemId);
        if (candidates == null) {
            return new Long2DoubleOpenHashMap();
        } else {
            Iterator<Map.Entry<Long, Double>> iter = candidates.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Long, Double> entry = iter.next();
                long item = entry.getKey();
                double similarity = entry.getValue();
                accum.add(item, similarity);
            }
            results = accum.finish();
            Long2DoubleMap nbrs = new Long2DoubleOpenHashMap();
            for (Result r : results) {
                long k = r.getId();
                double score = innerproducts.get(itemId).get(k);
                nbrs.put(r.getId(), score);
            }
            return nbrs;
        }
    }

}
