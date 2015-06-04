package org.lenskit.results;

import org.lenskit.api.Result;

import javax.annotation.concurrent.Immutable;
import java.io.*;

/**
 * A basic {@link Result} implementation with no details.
 *
 * @see Results
 */
@Immutable
public final class BasicResult extends AbstractResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Create a new basic result.
     * @param id The result ID.
     * @param score The result score.
     * @see Results#create(long, double)
     */
    public BasicResult(long id, double score) {
        super(id, score);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(id);
        out.writeDouble(score);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        id = in.readLong();
        score = in.readDouble();
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("basic result must have data");
    }

    /**
     * Compare this result with another for equality.  Instance of this result type are only equal with other basic
     * result instances; to compare general results for equality, first convert them to basic results with
     * {@link Results#basicCopy(Result)}.
     *
     * @param o The object to compare with.
     * @return `true` if the objects are equivalent.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o != null && o.getClass().equals(BasicResult.class)) {
            BasicResult or = (BasicResult) o;
            return startEquality(or).isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return startHashCode().toHashCode();
    }
}
