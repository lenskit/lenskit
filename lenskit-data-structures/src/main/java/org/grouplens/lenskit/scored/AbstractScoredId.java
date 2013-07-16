/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.scored;

import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

/**
 * A base class for {@code ScoredId} implementations providing
 * {@code equals} and {@code hashCode} methods.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public abstract class AbstractScoredId implements ScoredId {

    private transient volatile int hashCode;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            HashCodeBuilder builder = new HashCodeBuilder()
                    .append(getId())
                    .append(getScore());

            double sum = 0;
            for (Symbol s : getChannels()) {
                sum += s.hashCode();
                sum += Doubles.hashCode(channel(s));
            }
            builder.append(sum);
            
            sum = 0;
            for (TypedSymbol s : getTypedChannels()) {
                sum += s.hashCode();
                sum += channel(s).hashCode();
            }
            
            hashCode = builder.build();
        }
        return hashCode;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ScoredId) {
            ScoredId oid = (ScoredId) o;
            EqualsBuilder builder = new EqualsBuilder()
                    .append(getId(), oid.getId())
                    .append(getScore(), oid.getScore())
                    .append(getChannels(), oid.getChannels())
                    .append(getTypedChannels(), oid.getTypedChannels());

            // Try to avoid iterating through side channels if possible
            if (!builder.isEquals()) {
                return false;
            }

            for (Symbol s : getChannels()) {
                builder.append(channel(s), oid.channel(s));
            }
            
            for (TypedSymbol s : getTypedChannels()) {
                builder.append(channel(s), oid.channel(s));
            }
            return builder.isEquals();
        }
        return false;
    }
}