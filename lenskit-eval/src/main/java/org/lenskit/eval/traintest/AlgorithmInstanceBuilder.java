/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.lenskit.LenskitConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builder for algorithm instances.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class AlgorithmInstanceBuilder implements Builder<AlgorithmInstance> {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstanceBuilder.class);
    private AlgorithmInstanceBuilder parent;
    private LenskitConfiguration config;
    private String name;
    private Map<String, Object> attributes = new LinkedHashMap<>();

    /**
     * Construct a new algorithm instance builder.
     */
    public AlgorithmInstanceBuilder() {
        this("Unnamed");
    }

    /**
     * Construct a new algorithm instance builder with a name.
     * @param name The algorithm name.
     */
    public AlgorithmInstanceBuilder(String name) {
        this.name = name;
        attributes.put("Algorithm", name);
        config = new LenskitConfiguration();
    }

    private AlgorithmInstanceBuilder(AlgorithmInstanceBuilder parent) {
        this.parent = parent;
        name = parent.getName();
        config = new LenskitConfiguration();
        attributes = new LinkedHashMap<>();
    }

    /**
     * Set the algorithm name.
     *
     * @param n The name for this algorithm instance.
     * @return The command for chaining.
     */
    public AlgorithmInstanceBuilder setName(String n) {
        name = n;
        attributes.put("Algorithm", name);
        return this;
    }

    /**
     * Get the algorithm name.
     *
     * @return The algorithm name.
     */
    public String getName() {
        if (name == null && parent != null) {
            return parent.getName();
        } else {
            return name;
        }
    }

    /**
     * Set an attribute for this algorithm instance. Used for distinguishing similar
     * instances in an algorithm family.
     *
     * @param attr  The attribute name.
     * @param value The attribute value.
     * @return The command for chaining.
     */
    public AlgorithmInstanceBuilder setAttribute(@Nonnull String attr, @Nonnull Object value) {
        Preconditions.checkNotNull(attr, "attribute names cannot be null");
        Preconditions.checkNotNull(value, "attribute values cannot be null");
        attributes.put(attr, value);
        return this;
    }

    /**
     * Get the attributes of this algorithm instance.
     *
     * @return A map of user-defined attributes for this algorithm instance, excluding attributes inherited from the
     * parent.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get all attributes of this algorithm instance.
     * @return A map of all attributes in this algorithm instance.
     */
    public Map<String,Object> getAllAttributes() {
        Map<String,Object> result;
        if (parent != null) {
            // we always copy, so we can start w/ parent attributes
            result = parent.getAllAttributes();
            result.putAll(attributes);
        } else {
            // make copy
            result = new LinkedHashMap<>(attributes);
        }
        return result;
    }

    /**
     * Get the LensKit configuration.
     * @return The LensKit configuration.
     */
    public LenskitConfiguration getConfig() {
        return config;
    }

    /**
     * Set the LensKit configuration to use.
     * @param cfg The configuration to use. This will override any non-inherited configuration.
     * @return The builder (for chaining).
     */
    public AlgorithmInstanceBuilder setConfig(LenskitConfiguration cfg) {
        config = cfg;
        return this;
    }

    /**
     * Get the list of configurations that comprise this instance.
     * @return The list of configurations comprising this instance.
     */
    public List<LenskitConfiguration> getConfigurations() {
        List<LenskitConfiguration> list;
        if (parent != null) {
            list = parent.getConfigurations();
        } else {
            list = new LinkedList<>();
        }
        list.add(config);
        return list;
    }

    /**
     * Create a new builder that inherits from this builder.  Changes to the returned builder will not affect this
     * builder, but subsequent changes to this builder will affect the inherited builder.
     *
     * @return The duplicate instance builder.
     */
    public AlgorithmInstanceBuilder extend() {
        AlgorithmInstanceBuilder aib = new AlgorithmInstanceBuilder(this);
        return aib;
    }

    @Override
    public AlgorithmInstance build() {
        return new AlgorithmInstance(getName(), getConfigurations(), getAllAttributes());
    }
}
