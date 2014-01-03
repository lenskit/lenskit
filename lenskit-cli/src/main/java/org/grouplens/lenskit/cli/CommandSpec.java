package org.grouplens.lenskit.cli;

import java.lang.annotation.*;

/**
 * Define the command-line interface for a command.  This specifies the name and help text for the
 * subcommand.
 *
 * @see Command
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandSpec {
    String name();
    String help() default "";
}
