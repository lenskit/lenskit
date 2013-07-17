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
package org.grouplens.lenskit.core;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.util.TypedProvider;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * A symbol-backed implementation of a provider.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SymbolProvider<T> implements TypedProvider<T>, Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final TypedSymbol<T> symbol;

    private SymbolProvider(@Nonnull TypedSymbol<T> sym) {
        Preconditions.checkNotNull(sym, "provider symbol");
        symbol = sym;
    }

    /**
     * Create a symbol provdier from a symbol.
     * @param sym The symbol.  The provider will report that it provides instances of the symbol's
     *            type, to facilitate appropriate subtype/supertype injection.
     * @param <T> The provider's type.
     * @return A provider that will provide components based on the symbol.
     */
    public static <T> SymbolProvider<T> of(TypedSymbol<T> sym) {
        return new SymbolProvider<T>(sym);
    }

    /**
     * Get the symbol.
     * @return The symbol.
     */
    public TypedSymbol<T> getSymbol() {
        return symbol;
    }

    @Override
    public Class<?> getProvidedType() {
        return symbol.getType();
    }

    @Override
    public T get() {
        // you can't actually provide yet.
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return symbol.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SymbolProvider) {
            SymbolProvider<?> op = (SymbolProvider<?>) o;
            return symbol.equals(op.symbol);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();
    }
}
