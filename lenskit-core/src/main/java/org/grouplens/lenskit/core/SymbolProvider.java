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
