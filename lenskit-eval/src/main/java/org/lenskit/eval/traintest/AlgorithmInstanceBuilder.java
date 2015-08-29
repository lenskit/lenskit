/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.core.LenskitConfiguration;
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
        config = new LenskitConfiguration();
    }

    private AlgorithmInstanceBuilder(AlgorithmInstanceBuilder parent) {
        this.parent = parent;
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
        AlgorithmInstanceBuilder aib = new AlgorithmInstanceBuilder(name);
        aib.attributes = new LinkedHashMap<>(attributes);
        aib.config = new LenskitConfiguration(config);
        return aib;
    }

    @Override
    public AlgorithmInstance build() {
        AlgorithmInstance instance = new AlgorithmInstance(getName(), getConfigurations(), getAllAttributes());
        return instance;
    }
}
