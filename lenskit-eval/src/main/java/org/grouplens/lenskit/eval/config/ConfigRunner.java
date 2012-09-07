package org.grouplens.lenskit.eval.config;

import java.lang.annotation.*;

/**
 * Specify the default runner to run a command. The default is
 * {@link DefaultCommandRunner}.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ConfigRunner {
    Class<? extends CommandRunner> value();
}
