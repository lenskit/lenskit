package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.context.RatingBuildContext;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provider for item-item models that builds them with a model modelBuilder.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private ItemItemModelBuilder modelBuilder;
    private RatingBuildContext buildContext;
    private RatingPredictor baselinePredictor;
    
    @Inject
    public ItemItemModelProvider(ItemItemModelBuilder bldr, RatingBuildContext context, RatingPredictor baseline) {
        modelBuilder = bldr;
        buildContext = context;
        baselinePredictor = baseline;
    }
    
    @Override
    public ItemItemModel get() {
        return modelBuilder.build(buildContext, baselinePredictor);
    }
}
