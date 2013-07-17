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

import com.google.common.collect.Maps;
import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.inject.Provider;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * A mapping of typed symbols to providers.  Used to reconfigure recommenders.  This is used instead
 * of using {@linkplain Map maps} directly so that type safety can be enforced.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SymbolMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<TypedSymbol<?>, Provider<?>> bindings;

    private SymbolMapping(Map<TypedSymbol<?>, Provider<?>> map) {
        bindings = map;
    }

    /**
     * Create a new symbol mapping builder.
     * @return A new symbol mapping builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static SymbolMapping empty() {
        return new SymbolMapping(Collections.<TypedSymbol<?>, Provider<?>>emptyMap());
    }

    /**
     * Get the provider for a symbol.
     * @param sym The symbol.
     * @param <T> The symbol's type.
     * @return A provider for this symbol, or {@code null} if the symbol is unbound.
     */
    @SuppressWarnings("unchecked")
    public <T> Provider<T> get(TypedSymbol<T> sym) {
        return (Provider<T>) bindings.get(sym);
    }

    /**
     * Builder for symbol mappings.
     */
    public static class Builder {
        private Map<TypedSymbol<?>, Provider<?>> bindings = Maps.newHashMap();

        /**
         * Map a symbol to a provdier.
         *
         * @param sym The symbol.
         * @param provider The provider to use.
         * @return The builder (for chaining).
         */
        public <T> Builder put(TypedSymbol<T> sym, Provider<? extends T> provider) {
            bindings.put(sym, provider);
            return this;
        }

        /**
         * Map a symbol to an object.
         *
         * @param sym The symbol.
         * @param object The object to use.
         * @return The builder (for chaining).
         */
        public <T> Builder put(TypedSymbol<T> sym, T object) {
            bindings.put(sym, Providers.of(object));
            return this;
        }

        /**
         * Build the symbol mapping.
         * @return The symbol mapping set up by this builder.
         */
        public SymbolMapping build() {
            return new SymbolMapping(bindings);
        }
    }
}
