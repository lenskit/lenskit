package org.lenskit.results;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.api.Result;

import java.io.Serializable;

/**
 * A basic {@link Result} implementation with no details.
 *
 * @see Results
 */
@Immutable
public class BasicResult implements Result, Serializable {
    private static final long serialVersionUID = 1L;

    protected final long id;
    protected final double score;

    public BasicResult(long id, double score) {
        this.id = id;
        this.score = score;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public boolean hasScore() {
        return !Double.isNaN(score);
    }

    /**
     * Compare this result with another for equality.  Instance of this result type are only equal with other basic
     * result instances; to compare general results for equality, first convert them to basic results with
     * {@link Results#basicCopy(Result)}.  Detailed results that extend this class **must** override both this method
     * and {@link #hashCode()}.
     *
     * @param o The object to compare with.
     * @return `true` if the objects are equivalent.
     */
    @Override
    public boolean equals(Object o) {
        assert getClass().equals(BasicResult.class): "subclass failed to override equals()";
        if (o == this) {
            return true;
        } else if (o != null && o.getClass().equals(BasicResult.class)) {
            BasicResult or = (BasicResult) o;
            return new EqualsBuilder().append(id, or.id)
                                      .append(score, or.score)
                                      .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        assert getClass().equals(BasicResult.class): "subclass failed to override hashCode()";
        return new HashCodeBuilder().append(id).append(score).toHashCode();
    }
}
