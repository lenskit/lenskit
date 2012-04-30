package org.grouplens.lenskit.norm;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nullable;

/**
 * Normalize a user's vector. This vector is typically a rating or purchase vector.
 * <p>
 *     This interface is esentially a user-aware version of {@link VectorNormalizer}. The
 *     default implementation, {@link DefaultUserVectorNormalizer}, delegates to a
 *     {@link VectorNormalizer}. Implement this interface directly to create a normalizer
 *     that is aware of the fact that it is normalizing a user and e.g. uses user properties
 *     outside the vector to aid in the normalization. Otherwise, use a context-sensitive
 *     binding of {@link VectorNormalizer} to configure the user vector normalizer:
 * </p>
 *
 * {@code
 * factory.in(UserVectorNormalizer.class)
 *        .bind(VectorNormalizer.class)
 *        .to(MeanVarianceNormalizer.class);
 * }
 *
 * @see VectorNormalizer
 * @author Michael Ekstrand
 * @since 0.11
 */
@DefaultImplementation(DefaultUserVectorNormalizer.class)
public interface UserVectorNormalizer {
    /**
     * Normalize a vector with respect to a user vector.
     * @param user The user vector to use as a reference.
     * @param target The vector to normalize. If {@code null}, the user vector is normalized.
     * @return The {@code target} vector, if specified. Otherwise, a fresh mutable vector
     *         containing a normalized copy of the user vector is returned.
     */
    MutableSparseVector normalize(UserVector user,
                                  @Nullable MutableSparseVector target);

    /**
     * Make a vector transformation for a user. The resulting transformation will be applied
     * to user vectors to normalize and denormalize them.
     * @param user The user to normalize for.
     * @return The vector transformaition normalizing for this user.
     */
    VectorTransformation makeTransformation(UserVector user);
}
