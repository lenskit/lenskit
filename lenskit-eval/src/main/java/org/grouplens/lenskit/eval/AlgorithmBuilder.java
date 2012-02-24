package org.grouplens.lenskit.eval;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.eval.config.AlgorithmBuilderDelegate;
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.config.ConfigDelegate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for algorithm instances.
 * @author Michael Ekstrand
 */
@ConfigDelegate(AlgorithmBuilderDelegate.class)
public class AlgorithmBuilder implements Builder<AlgorithmInstance> {
    private String name;
    private Map<String,Object> attributes = new HashMap<String, Object>();
    private boolean preload;
    private LenskitRecommenderEngineFactory factory;

    public AlgorithmBuilder() {
        factory = new LenskitRecommenderEngineFactory();
    }

    public AlgorithmBuilder(String name) {
        this();
        this.name = name;
    }

    /**
     * Get the algorithm name.
     * @return The name for this algorithm instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the algorithm name.
     * @param n The name for this algorithm instance.
     * @return The builder for chaining.
     */
    public AlgorithmBuilder setName(String n) {
        name = n;
        return this;
    }

    /**
     * Get whether this algorithm will require ratings to be pre-loaded.
     * @return {@code true} if the algorithm should have ratings pre-loaded into memory.
     */
    public Boolean getPreload() {
        return preload;
    }

    /**
     * Set whether the algorithm wants ratings pre-loaded. Use this for algorithms that
     * are too slow reading on a CSV file if you have enough memory to load them all.
     * @param pl {@code true} to pre-load input data when running this algorithm.
     * @return The builder for chaining.
     */
    public AlgorithmBuilder setPreload(boolean pl) {
        preload = pl;
        return this;
    }

    /**
     * Set an attribute for this algorithm instance. Used for distinguishing similar
     * instances in an algorithm family.
     * @param attr The attribute name.
     * @param value The attribute value.
     * @return The builder for chaining.
     */
    public AlgorithmBuilder setAttribute(String attr, Object value) {
        attributes.put(attr, value);
        return this;
    }

    /**
     * Get the attributes of this algorithm instance.
     * @return A map of user-defined attributes for this algorithm instance.
     */
    public Map<String,Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the factory for this instance.
     * @return The factory for this recommender instance. Each instance has the factory
     * instantiated to a fresh, empty factory.
     */
    public LenskitRecommenderEngineFactory getFactory() {
        return factory;
    }

    @Override
    public AlgorithmInstance build() {
        return new AlgorithmInstance(name, factory, attributes, preload);
    }

    @ServiceProvider
    public static class Factory implements BuilderFactory<AlgorithmInstance> {
        public String getName() {
            return "algorithm";
        }
        @Nonnull
        public AlgorithmBuilder newBuilder(String arg) {
            return new AlgorithmBuilder(arg);
        }
    }
}
