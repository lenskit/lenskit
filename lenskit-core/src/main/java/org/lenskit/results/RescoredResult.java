package org.lenskit.results;

import org.lenskit.api.Result;

import javax.annotation.Nonnull;

/**
 * A result that wraps another result with a different score.
 */
public final class RescoredResult extends AbstractResult {
    private final Result inner;

    public RescoredResult(Result base, double val) {
        super(base.getId(), val);
        inner = base;
    }

    public Result getInnerResult() {
        return inner;
    }

    @Override
    public <T extends Result> T as(@Nonnull Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        } else {
            return inner.as(type);
        }
    }

    @Override
    public int hashCode() {
        return startHashCode().append(inner).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RescoredResult) {
            RescoredResult cr = (RescoredResult) obj;
            return startEquality(cr).append(inner, cr.getInnerResult()).build();
        } else {
            return false;
        }
    }
}
