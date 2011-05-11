package org.grouplens.lenskit;


public interface Builder<M> {
    /**
     * Build the model object of type M using the Builder's current
     * configuration.
     * 
     * @return An instance of type M built with the builder's configuration
     */
    public abstract M build();
}
