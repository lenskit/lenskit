package org.grouplens.lenskit.eval.config;

import groovy.lang.Closure;
import org.grouplens.lenskit.eval.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Invoke a configuration block.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
public interface CommandRunner {
    /**
     * Invoke a command, possibly using a configuration closure.
     * @param command The command to run.
     * @param closure The closure to configure it with.
     * @param <V> The return type of the command.
     * @return The results of the command's {@link Command#call()} method.
     */
    <V> V invoke(@Nonnull Command<V> command, @Nullable Closure<?> closure);
}
