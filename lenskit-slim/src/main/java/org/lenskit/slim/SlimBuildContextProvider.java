package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.inject.Transient;
import org.lenskit.util.IdBox;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SlimBuildContextProvider implements Provider<SlimBuildContext> {
    private final DataAccessObject dao;

    /**
     * Construct the model provider.
     * @param dao The data access object.
     */
    @Inject
    public SlimBuildContextProvider(@Transient DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * Construct the data objects needed by building slim model.
     * @return The slimBuildContext.
     */
    @Override
    public SlimBuildContext get() {
        Map<Long,Long2DoubleMap> itemVectors = Maps.newHashMap();
        Long2ObjectMap<LongOpenHashBigSet> userItems = new Long2ObjectOpenHashMap<>();
//        Long2DoubleMap itemMeans = new Long2DoubleOpenHashMap();

        try (ObjectStream<IdBox<List<Rating>>> stream = dao.query(Rating.class)
                                                           .groupBy(CommonAttributes.ITEM_ID)
                                                           .stream()) {
            for (IdBox<List<Rating>> item : stream) {
                long itemId = item.getId();
                List<Rating> itemRatings = item.getValue();
                Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));

                LongIterator iter = ratings.keySet().iterator();
                while (iter.hasNext()) {
                    long userId = iter.nextLong();
                    LongOpenHashBigSet userRatedItems = userItems.get(userId);
                    if (userRatedItems == null) userRatedItems = new LongOpenHashBigSet();
                    userRatedItems.add(itemId);
                    userItems.put(userId, userRatedItems);
                }
                itemVectors.put(itemId, LongUtils.frozenMap(ratings));
            }
        }

        // Map items to vectors (maps) of item inner-product, which used to speed up slim learning process.
        Map<Long,Long2DoubleMap> innerProducts = Maps.newHashMap();
        LongOpenHashBigSet itemIdSet = new LongOpenHashBigSet(itemVectors.keySet());
        Iterator<Map.Entry<Long, Long2DoubleMap>> iter = itemVectors.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Long, Long2DoubleMap> entry = iter.next();
            long temIId = entry.getKey();
            Long2DoubleMap itemIRatings = entry.getValue();
            itemIdSet.remove(temIId);

            for (long itemJId : itemIdSet) {
                Long2DoubleMap itemJRatings = itemVectors.get(itemJId);
                double innerProduct = Vectors.dotProduct(itemIRatings, itemJRatings);

                // storing interProducts used for SLIM learning
                Long2DoubleMap dotJIs = innerProducts.get(itemJId);
                Long2DoubleMap dotIJs = innerProducts.get(temIId);
                if (dotJIs == null) dotJIs = new Long2DoubleOpenHashMap();
                if (dotIJs == null) dotIJs = new Long2DoubleOpenHashMap();
                dotJIs.put(temIId, innerProduct);
                dotIJs.put(itemJId, innerProduct);
                innerProducts.put(itemJId, dotJIs);
                innerProducts.put(temIId, dotIJs);
            }
        }

        return new SlimBuildContext(itemVectors, innerProducts, userItems);
    }
}
