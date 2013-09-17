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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.symbols.DoubleSymbolValue;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.SymbolValue;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * A base class for {@code ScoredId} implementations providing
 * {@code equals} and {@code hashCode} methods.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 * @compat Public
 */
public abstract class AbstractScoredId implements ScoredId {

    private transient volatile int hashCode;
    private transient volatile String stringRepr;

    @Override
    public String toString() {
        if (stringRepr == null) {
            StringBuilder bld = new StringBuilder();
            bld.append("score(")
               .append(getId())
               .append(") = ")
               .append(getScore());
            int nchans = getChannels().size();
            if (nchans > 0) {
                bld.append(" [with ").append(nchans).append(" channels]");
            }
            stringRepr = bld.toString();
        }
        return stringRepr;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            HashCodeBuilder builder = new HashCodeBuilder()
                    .append(getId())
                    .append(getScore());

            double sum = 0;
            // FIXME Don't incur the boxing cost of doing this to double side channels
            for (SymbolValue<?> sym: getChannels()) {
                sum += sym.hashCode();
            }
            builder.append(sum);
            
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
            Ordering<SymbolValue<?>> ord = Ordering.arbitrary()
                                                   .onResultOf(SymbolValue.extractSymbol());
            return new EqualsBuilder()
                    .append(getId(), oid.getId())
                    .append(getScore(), oid.getScore())
                            // FIXME Don't incur the boxing cost of doing this to double side channels
                    .append(ord.sortedCopy(getChannels()),
                            ord.sortedCopy(oid.getChannels()))
                    .isEquals();
        }
        return false;
    }

    @Nonnull
    @Override
    public Collection<DoubleSymbolValue> getUnboxedChannels() {
        return FluentIterable.from(getChannels())
                             .filter(DoubleSymbolValue.class)
                             .toList();
    }

    @Override
    public Set<TypedSymbol<?>> getChannelSymbols() {
        ImmutableSet.Builder<TypedSymbol<?>> builder = ImmutableSet.builder();
        for (SymbolValue<?> sv: getChannels()) {
            builder.add(sv.getSymbol());
        }
        return builder.build();
    }

    @Override
    public Set<Symbol> getUnboxedChannelSymbols() {
        ImmutableSet.Builder<Symbol> builder = ImmutableSet.builder();
        for (SymbolValue<?> sv: getUnboxedChannels()) {
            builder.add(sv.getRawSymbol());
        }
        return builder.build();
    }

    /**
     * {@inheritDoc}
     *
     * This implementation scans the result of {@link #getChannels()} for a matching channel.
     */
    @Override
    public boolean hasChannel(TypedSymbol<?> sym) {
        for (SymbolValue<?> val: getChannels()) {
            if (sym == val.getSymbol()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * This implementation delgates to {@link #hasChannel(TypedSymbol)}.
     * @param sym
     * @return
     */
    @Override
    public boolean hasUnboxedChannel(Symbol sym) {
        return hasChannel(sym.withType(Double.class));
    }
}