/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

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
 * Symbols are NOT guaranteed to return the same internal value each
 * time they are used.  If the Symbol is serialized, the string should
 * be stored, NOT the internal value underlying the Symbol.
 * <p>
 * Symbols are intended to be efficient, but they are implemented
 * assuming there will not be *too* many of them.  If your program
 * needs thousands, the implementation should be changed.
 * <p>
 * Symbols cannot be constructed, but must be fetched through the "of"
 * operator.
 * <p>
 * Symbols are hashable, because they are singletons, so the default hashCode
 * based on Object address should work.
 *
 * @author John Riedl <riedl@cs.umn.edu>
 * @compat Public
 */
public final class Symbol {
    // Variables shared by all instances
    private static Map<String, Symbol> name2SymbolMap = new Reference2ObjectArrayMap<String, Symbol>();

    // Variables unique to each instance
    private final String strSymbol;    // The name of the symbol, which is the string used to create it.

    // The only constructors are private, so Symbols can only be created
    // through the public interface.
    private Symbol() {
        this("");
    }

    // The only constructor is private, so Symbols can only be created
    // through the public interface.
    private Symbol(String name) {
        strSymbol = name;
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
     * Get the name for a symbol
     *
     * @return the string name that was used to create the symbol
     */
    public synchronized String getName() {
        return this.strSymbol;
    }

    @Override
    public String toString() {
        return String.format("Symbol.of(%s)", this.getName());
    }

}
