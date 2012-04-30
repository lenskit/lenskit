package org.grouplens.lenskit.norm;

import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Default user vector normalizer that delegates to a generic {@link VectorNormalizer}.
 * @author Michael Ekstrand
 * @since 0.11
 */
public class DefaultUserVectorNormalizer implements UserVectorNormalizer, Serializable {
    private static final long serialVersionUID = 1L;
    protected final VectorNormalizer delegate;

    /**
     * Construct a new user vector normalizer that uses the identity normalization.
     */
    public DefaultUserVectorNormalizer() {
        this(new IdentityVectorNormalizer());
    }

    /**
     * Construct a new user vector normalizer wrapping a generic vector normalizer.
     * @param norm The generic normalizer to use.
     */
    @Inject
    public DefaultUserVectorNormalizer(VectorNormalizer norm) {
        delegate = norm;
    }

    public MutableSparseVector normalize(UserVector user, @Nullable MutableSparseVector target) {
        return delegate.normalize(user, target);
    }

    public VectorTransformation makeTransformation(UserVector user) {
        return delegate.makeTransformation(user);
    }
}
