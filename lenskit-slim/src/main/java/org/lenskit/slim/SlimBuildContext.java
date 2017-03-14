package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(SlimBuildContextProvider.class)
public class SlimBuildContext implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final
    Map<Long,Long2DoubleMap> itemVectors;
    @Nonnull
    private final
    Map<Long,Long2DoubleMap> innerProducts;
    @Nonnull
    private final Long2ObjectMap<LongOpenHashBigSet> userItems;


    /**
     * Set up a new item build context
     *
     * @param itemRatings Map of item IDs to item rating vectors.
     * @param innerProducts Map of item IDs to inner-products with other item rating vectors
     * @param userItems Map of user IDs to user rated items
     */
    public SlimBuildContext(@Nonnull Map<Long,Long2DoubleMap> itemRatings,
                            @Nonnull Map<Long,Long2DoubleMap> innerProducts,
                            @Nonnull Long2ObjectMap<LongOpenHashBigSet> userItems) {
        itemVectors = itemRatings;
        this.innerProducts = innerProducts;
        this.userItems = userItems;
    }


    /**
     * Get all neighbors' inner-products with the given item id
     * @param itemId The item id to query for
     * @return a mapping of neighborhoods' id to inner-products
     */
    @Nonnull
    public Long2DoubleMap getInnerProducts(long itemId) {
        Long2DoubleMap innerProduct = innerProducts.get(itemId);
        if (innerProduct == null) {
            innerProduct = new Long2DoubleOpenHashMap();
        }
        return innerProduct;
    }

    @Nonnull
    public Map<Long,Long2DoubleMap> getInnerProducts() { return innerProducts;}

    /**
     * Get the items rated by a particular user
     * @param user The user to query for
     * @return The items rated by {@code user}
     */
    @Nonnull
    public LongOpenHashBigSet getUserItems(long user) {
        LongOpenHashBigSet items = userItems.get(user);
        if (items == null) {
            items = new LongOpenHashBigSet();
        }
        return items;
    }

    @Nonnull
    public Long2DoubleMap getItemRatings(long item) {
        Long2DoubleMap itemRatings = itemVectors.get(item);
        if (itemRatings == null) {
            itemRatings = new Long2DoubleOpenHashMap();
        }
        return itemRatings;
    }

    /**
     * Get a rating matrix.
     *
     * @return Map of item IDs to item rating vectors.
     */
    @Nonnull
    public Map<Long,Long2DoubleMap> getItemVectors() {
        return itemVectors;
    }


}
