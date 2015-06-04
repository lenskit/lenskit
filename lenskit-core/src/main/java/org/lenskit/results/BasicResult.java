/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.results;

import com.google.common.base.MoreObjects;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("score", score)
                          .toString();
    }
}
