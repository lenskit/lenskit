package org.grouplens.lenskit.eval;


import javax.annotation.Nonnull;

/**
 * The abstract class of Command.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public abstract class AbstractCommand<T> implements Command<T> {
    protected String name;

    public AbstractCommand() {
        this.name = "Not specified";
    }

    public AbstractCommand(@Nonnull String name) {
            this.name = name;
    }

    public AbstractCommand setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract T call() throws CommandFailedException;
}
