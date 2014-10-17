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

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

/**
 * A symbol associated with a particular type.  Conceptually, each type can have a collection of
 * symbols associated with it; that association is represented represented by this class.
 *
 * @since 1.3
 * @see Symbol
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class TypedSymbol<K> implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("rawtypes")
    private static Map<Pair<Class,Symbol>,TypedSymbol> symbolCache = Maps.newHashMap();

    private final Class<K> type;
    private final Symbol symbol;

    // The only constructor is private, so Symbols can only be created
    // through the public interface.
    private TypedSymbol(Class<K> clazz, Symbol sym) {
        type = clazz;
        symbol = sym;
    }

    private Object readResolve() throws ObjectStreamException {
        return of(type, symbol);
    }

    /**
     * Get a unique symbol for {@var name} and {@var type}.
     *
     * @param type The type for the type-symbol pair.
     * @param name The name for the type-symbol pair.
     * @return the new symbol
     */
    @SuppressWarnings("unchecked")
    public static <T> TypedSymbol<T> of(Class<T> type, String name) {
        return of(type, Symbol.of(name));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static synchronized <T> TypedSymbol<T> of(Class<T> type, Symbol sym) {
        Pair<Class,Symbol> key;
        if (type.isPrimitive()) {
            key = Pair.of((Class) ClassUtils.primitiveToWrapper(type), sym);
        } else {
            key = Pair.of((Class) type, sym);
        }
        TypedSymbol tsym = symbolCache.get(key);
        if (tsym == null) {
            if (type.isPrimitive()) {
                type = (Class<T>) ClassUtils.primitiveToWrapper(type);
            }
            tsym = new TypedSymbol<T>(type, sym);
            symbolCache.put(key, tsym);
        }
        return tsym;
    }

    /**
     * Get the name for a symbol.
     *
     * @return the string name that was used to create the symbol
     */
    public String getName() {
        return symbol.getName();
    }
    
    /**
     * Get the type for a typed symbol.
     * 
     * @return the type that was used to create the symbol.
     */
    public Class<K> getType() {
        return type;
    }

    /**
     * Get the symbol.
     * @return The symbol associated with the type.
     */
    public Symbol getRawSymbol() {
        return symbol;
    }

    /**
     * Create a value paired with this symbol.
     * @param val The value.
     * @return A pair of this symbol and the value.
     */
    public SymbolValue<K> withValue(K val) {
        return SymbolValue.of(this, val);
    }

    @Override
    public String toString() {
        return String.format("TypedSymbol.of(%s,%s)", getType().getSimpleName(), getName());
    }
}
