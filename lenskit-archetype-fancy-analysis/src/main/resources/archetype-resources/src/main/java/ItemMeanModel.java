/* This file may be freely modified, used, and redistributed without restriction. */
package ${package};

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Serializable;

/**
 * Model that maintains the mean offset from the global mean for the ratings
 * for each item.
 *
 * These offsets can be used for predictions by calling the {@link #getGlobalMean()}
 * and {@link #getItemOffsets()} methods.
 *
 * <p>These computations support mean smoothing (see {@link MeanDamping}).
 *
 * Users of this model will usually call the Provider's get method to create
 * a suitable model.  The model can be kept around until recomputation is necessary.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(ItemMeanModelBuilder.class)
public class ItemMeanModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double globalMean;
    private final ImmutableSparseVector itemOffsets;

    public ItemMeanModel(double global, SparseVector items) {
        itemOffsets = items.immutable();
        globalMean = global;
    }

    /**
     * Get the global mean rating.
     * @return The global mean rating.
     */
    public double getGlobalMean() {
        return globalMean;
    }

    /**
     * Get the vector of item mean offsets.
     *
     * @return The vector of item mean offsets.  These are mean offsets from the global mean rating,
     *         so add the global mean to each rating to get the item mean.
     */
    public ImmutableSparseVector getItemOffsets() {
        return itemOffsets;
    }

}
