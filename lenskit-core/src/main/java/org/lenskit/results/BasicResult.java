/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.results;

import com.google.common.base.MoreObjects;
import org.lenskit.api.Result;

import net.jcip.annotations.Immutable;
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

    //region Serialization support
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
    //endregion

    //region Value object behavior
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

    @Override
    public String toString() {
        return String.format("<%d:%.4f>", id, score);
    }
    //endregion
}
