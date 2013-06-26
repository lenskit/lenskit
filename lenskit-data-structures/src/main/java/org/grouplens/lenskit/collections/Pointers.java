package org.grouplens.lenskit.collections;

import com.google.common.base.Function;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Pointers {
    private Pointers() {}

    /**
     * Transform a pointer.
     * @param pointer The pointer to transform.
     * @param func A function to apply.  This function is called on each call to {@link Pointer#get()}}.
     * @param <T> The type of the underlying pointer.
     * @param <R> The type of the transformed pointer.
     * @return A transformed pointer.
     */
    public static <T,R> Pointer<R> transform(Pointer<T> pointer, Function<? super T, ? extends R> func) {
        return new TransformedPointer<T,R>(pointer, func);
    }

    private static class TransformedPointer<T,R> implements Pointer<R> {
        private final Pointer<T> pointer;
        private final Function<? super T, ? extends R> function;

        public TransformedPointer(Pointer<T> ptr, Function<? super T, ? extends R> func) {
            pointer = ptr;
            function = func;
        }

        @Override
        public boolean advance() {
            return pointer.advance();
        }

        @Override
        public R get() {
            return function.apply(pointer.get());
        }

        @Override
        public boolean isAtEnd() {
            return pointer.isAtEnd();
        }
    }
}
