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
package org.grouplens.lenskit.symbols;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to persistent symbols with associated type information.
 *
 * <p>
 * A Symbol is an inexpensive object that behaves like a singleton --
 * except that you can create as many as you want.  Each TypedSymbol 
 * is created with a String and a class, and a unique Symbol is assigned 
 * to this pair.  Any fetches of that String and class will return the same Symbol.
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
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class TypedSymbol<K> {
    
    // Variables shared by all instances
    private static List<TypedSymbol<?>> typedSymbols = new ArrayList<TypedSymbol<?>>();
    
    // Variables unique to each instance
    private final String strSymbol;    // The name of the symbol, which is the string used to create it.
    private final Class<K> type;    // The type of the symbol

    // The only constructors are private, so Symbols can only be created
    // through the public interface.
    private TypedSymbol() {
        this("",null);
    }

    // The only constructor is private, so Symbols can only be created
    // through the public interface.
    private TypedSymbol(String name, Class<K> clazz) {
        strSymbol = name;
        type = clazz;
    }


    /**
     * Get a unique symbol for {@var name} and {@var type}.
     *
     * @param name the name for this symbol during this execution of
     *             the program
     * @param type the type for this symbol during this execution of
     *             the program
     * @return the new symbol
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> TypedSymbol<T> of(String name, Class<T> type) {
        for(TypedSymbol<?> ts : typedSymbols) {
            if(ts.getName().equals(name) && ts.getType().equals(type)) {
                return (TypedSymbol<T>) ts;
            }
        }
        TypedSymbol<T> newSymbol = new TypedSymbol<T>(name, type);
        typedSymbols.add(newSymbol);
        return newSymbol;
    }

    /**
     * Get the name for a symbol.
     *
     * @return the string name that was used to create the symbol
     */
    public synchronized String getName() {
        return this.strSymbol;
    }
    
    /**
     * Get the type for a symbol
     * 
     * @return the type that was used to create the symbol.
     */
    public Class<K> getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("TypedSymbol.of(%s,%s)", this.getName(), this.getType().getSimpleName());
    }    
}
