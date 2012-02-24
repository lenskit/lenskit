package org.grouplens.lenskit.eval.config;

import java.lang.annotation.*;

/**
 * Specify a delegate class to be used to configure builders of the type to which
 * this annotation is applied. When using a builder to instantiate a setter or adder
 * parameter, if the builder class has this annotation the specified class is used
 * as the delegate instead of the default {@link BuilderDelegate} when invoking the
 * builder closure.
 * <p/>
 * If the delegate class has a constructor taking the builder as a single argument,
 * that constructor is used; otherwise a no-arg constructor is used. The builder
 * constructor is highly recommended, as otherwise there isn't a good way to make
 * the builder available to the delegate.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigDelegate {
    /**
     * The delegate implementation to use. The class must have a no-arg public
     * constructor.
     * @return The delegate class.
     */
    Class<?> value();
}
