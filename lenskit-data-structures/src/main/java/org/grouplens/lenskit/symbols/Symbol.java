/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.collect.Maps;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

/**
 * Interface to persistent symbols.
 *
 * <p>
 * A Symbol is an inexpensive object that behaves like a singleton --
 * except that you can create as many as you want.  Each Symbol is
 * created with a String, and a unique Symbol is assigned to this
 * String.  Any fetches of that String will return the same Symbol.
 * <p>
 * Symbols cannot be constructed, but must be fetched through the "of"
 * operator.
 * <p>
 * Symbols are hashable, because they are singletons, so the default hashCode
 * based on Object address should work.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class Symbol implements Serializable {
    // Variables shared by all instances
    private static final Map<String, Symbol> name2SymbolMap = Maps.newHashMap();
    private static final long serialVersionUID = 1L;

    // Variables unique to each instance
    private final String name;    // The name of the symbol, which is the string used to create it.

    // The only constructor is private, so Symbols can only be created
    // through the public interface.
    private Symbol(String name) {
        this.name = name;
    }

    private Object readResolve() throws ObjectStreamException {
        return of(name);
    }

    /**
     * Get a unique symbol for {@var name}.
     *
     * @param name the name for this symbol during this execution of
     *             the program
     * @return the new symbol
     */
    public static synchronized Symbol of(String name) {
        if (name2SymbolMap.containsKey(name)) {
            return name2SymbolMap.get(name);
        }
        Symbol newSymbol = new Symbol(name);
        name2SymbolMap.put(name, newSymbol);
        return newSymbol;
    }

    /**
     * Get the name for a symbol.
     *
     * @return the string name that was used to create the symbol
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Symbol.of(%s)", this.getName());
    }

    /**
     * Make a typed symbol from this symbol.
     * @param type The type.
     * @param <T> The type.
     * @return A typed symbol with the specified type and this symbol.
     */
    public <T> TypedSymbol<T> withType(Class<T> type) {
        return TypedSymbol.of(type, this);
    }
}
