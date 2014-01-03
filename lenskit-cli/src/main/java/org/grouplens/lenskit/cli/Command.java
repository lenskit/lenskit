package org.grouplens.lenskit.cli;

/**
 * Interface implemented by all CLI subcommands.
 *
 * <p>In addition to implementing this interface, a subcommand must be annotated with {@link CommandSpec}
 * to declare its name and help text, and have a static method {@code configureArguments(ArgumentParser)}
 * that registers the subcommand's arguments and options with the provided argument parser.</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface Command {
    /**
     * Execute the command.
     *
     * @throws Exception if an error occurs in the command.
     */
    void execute() throws Exception;
}
