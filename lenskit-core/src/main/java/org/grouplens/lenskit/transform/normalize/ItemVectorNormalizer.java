package org.grouplens.lenskit.transform.normalize;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Normalizes an item's vector.
 */
@DefaultImplementation(DefaultItemVectorNormalizer.class)
public interface ItemVectorNormalizer {
    /**
     * Normalize a vector with respect to an item vector.
     *
     * @param itemId   The item id to normalize a vector for.
     * @param vector The item's vector for reference.
     * @param target The vector to normalize. If {@code null}, the item vector is normalized.
     * @return The {@code target} vector, if specified. Otherwise, a fresh mutable vector
     *         containing a normalized copy of the item vector is returned.
     */
    MutableSparseVector normalize(long itemId, @Nonnull SparseVector vector,
                                  @Nullable MutableSparseVector target);

    /**
     * Make a vector transformation for an item. The resulting transformation will be applied
     * to item vectors to normalize and denormalize them.
     *
     * @param itemId   The item id to normalize for.
     * @param vector The item's vector to use as the reference vector.
     * @return The vector transformaition normalizing for this item.
     */
    VectorTransformation makeTransformation(long itemId, SparseVector vector);
}
