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
package org.grouplens.lenskit.symbols;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;

/**
 * A pairing of a {@link TypedSymbol} with a value of the same type.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class SymbolValue<T> {
    /**
     * Create a typed symbol value.
     * @param sym The symbol.
     * @param val The value.
     * @param <T> The type of the symbol.
     * @return A pair of the symbol and value.
     */
    @SuppressWarnings("unchecked")
    public static <T> SymbolValue<T> of(TypedSymbol<T> sym, T val) {
        if(sym.getType().equals(Double.class)) {
            return (SymbolValue<T>) new DoubleSymbolValue((TypedSymbol<Double>) sym, (Double) val);
        } else {
            return new TypedSymbolValue<T>(sym, val);
        }
    }

    /**
     * Create an unboxed symbol value.
     * @param sym The symbol.
     * @param val The value.
     * @return A pair of the symbol and value.
     */
    public static DoubleSymbolValue of(TypedSymbol<Double> sym, double val) {
        return new DoubleSymbolValue(sym, val);
    }

    /**
     * Create an unboxed symbol value.
     * @param sym The symbol.
     * @param val The value.
     * @return A pair of the symbol and value.
     */
    public static DoubleSymbolValue of(Symbol sym, double val) {
        return of(TypedSymbol.of(Double.class, sym), val);
    }

    SymbolValue() {}

    public abstract TypedSymbol<T> getSymbol();

    /**
     * Get the raw {@link Symbol} from the symbol value.
     * @return The raw {@link Symbol}.
     */
    public Symbol getRawSymbol() {
        return getSymbol().getRawSymbol();
    }

    public abstract T getValue();

    @Override
    public boolean equals(Object other) {
        if (other instanceof SymbolValue) {
            SymbolValue<?> sv = (SymbolValue<?>) other;
            if (getSymbol() != sv.getSymbol()) {
                return false;
            }
            assert getSymbol().getClass().equals(sv.getSymbol().getClass());
            return getValue().equals(sv.getValue());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(getSymbol())
                  .append(getValue())
                  .toHashCode();
    }

    public static Predicate<SymbolValue<?>> hasSymbol(final TypedSymbol<?> sym) {
        return new Predicate<SymbolValue<?>>() {
            @Override
            public boolean apply(@Nullable SymbolValue<?> input) {
                return input != null && input.getSymbol() == sym;
            }
        };
    }

    public static Function<SymbolValue<?>,TypedSymbol<?>> extractSymbol() {
        return SymbolFunc.INSTANCE;
    }

    static enum SymbolFunc implements Function<SymbolValue<?>,TypedSymbol<?>> {
        INSTANCE {
            @Nullable
            @Override
            public TypedSymbol<?> apply(@Nullable SymbolValue<?> input) {
                if (input == null) {
                    return null;
                } else {
                    return input.getSymbol();
                }
            }
        }
    }
}
