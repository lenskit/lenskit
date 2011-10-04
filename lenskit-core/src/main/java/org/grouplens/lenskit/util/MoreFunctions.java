package org.grouplens.lenskit.util;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Additional function utilities to go with {@link Functions}.
 * 
 * @since 0.9
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class MoreFunctions {
    
    /**
     * Identity function casting its arguments to a particular type.
     * 
     * @param target The target type for arguments.
     * @return A function which, when applied to an object, casts it to type
     *         <var>target</var>.
     */
    public static <F,T> Function<F,T> cast(final Class<T> target) {
        return new Function<F,T>() {
            @Override
            public T apply(F obj) {
                return target.cast(obj);
            }
        };
    }
}
