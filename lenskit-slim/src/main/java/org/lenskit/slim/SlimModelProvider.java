package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;



public class SlimModelProvider implements Provider<SlimModel>{
    private static final Logger logger = LoggerFactory.getLogger(SlimModelProvider.class);

    private final ItemItemModel itemSimilarityModel;
    private final SlimBuildContext buildContext;
    private final AbstractLinearRegression lrModel;

    @Inject
    public SlimModelProvider(ItemItemModel m,
                             @Transient SlimBuildContext context,
                             AbstractLinearRegression regressionModel) {
        itemSimilarityModel = m;
        buildContext = context;
        lrModel = regressionModel;
    }

    @Override
    public SlimModel get() {
        Long2ObjectMap<Long2DoubleMap> trainedWeight = new Long2ObjectOpenHashMap<>(1000);
        LongSortedSet items = itemSimilarityModel.getItemUniverse();
        for (long item : items) {
            LongSet neighbors = LongUtils.frozenSet(itemSimilarityModel.getNeighbors(item).keySet());
            Map<Long,Long2DoubleMap> trainingData = Maps.newHashMap();
            Map<Long,Long2DoubleMap> innerProducts = Maps.newHashMap();
            Long2DoubleMap labels = LongUtils.frozenMap(buildContext.getItemRatings(item));
            for (long nbrs : neighbors) {
                trainingData.put(nbrs, LongUtils.frozenMap(buildContext.getItemRatings(nbrs)));
                innerProducts.put(nbrs, LongUtils.frozenMap(buildContext.getInnerProducts(nbrs)));
            }
            Long2DoubleMap weight = lrModel.fit(labels, trainingData, innerProducts, item);
            trainedWeight.put(item, weight);
        }
        return new SlimModel(trainedWeight);
    }
}
