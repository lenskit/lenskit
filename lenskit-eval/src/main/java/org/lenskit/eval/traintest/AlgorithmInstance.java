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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.lenskit.LenskitConfiguration;
import org.lenskit.config.ConfigurationLoader;
import org.lenskit.config.LenskitConfigScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of a recommender algorithm to be trained and measured.
 */
public class AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    @Nullable
    private final String algoName;
    @Nonnull
    private final List<LenskitConfiguration> configurations;
    @Nonnull
    private final Map<String, Object> attributes;

    /**
     * Construct a new algorithm instance.
     * @param name The algorithm name.
     * @param config The algorithm configuration.
     */
    public AlgorithmInstance(String name, LenskitConfiguration config) {
        this(name, ImmutableList.of(config),
             ImmutableMap.<String,Object>of());
    }

    /**
     * Construct a new algorithm instance.
     * @param name The algorithm name.
     * @param configs The algorithm configurations.
     * @param attrs The attributes for this algorithm instance.
     */
    public AlgorithmInstance(String name, List<LenskitConfiguration> configs, Map<String, Object> attrs) {
        algoName = name;
        configurations = ImmutableList.copyOf(configs);
        attributes = ImmutableMap.copyOf(attrs);
    }


    /**
     * Get the name of this algorithm.  This returns a short name which is
     * used to identify the algorithm or instance.
     *
     * @return The algorithm's name
     */
    public String getName() {
        return algoName;
    }

    /**
     * Get the attributes of this algorithm.  These attributes are used to distinguish between similar algorithms,
     * e.g. different configurations of the same basic algorithm.
     * @return The algorithm attributes.
     */
    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the recommender configurations.
     * @return
     */
    @Nonnull
    public List<LenskitConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Load an algorithm instance from a file.
     *
     * @param file The file to load.
     * @param name The algorithm name, or `null` to use the file's basename.
     * @param classLoader The class loader, or `null` to use a default.
     *
     * @return The list of loaded algorithm instances.
     */
    public static List<AlgorithmInstance> load(Path file, String name, ClassLoader classLoader) {
        ConfigurationLoader loader = new ConfigurationLoader(classLoader);
        AlgorithmInstanceBuilder aib = new AlgorithmInstanceBuilder(name);
        if (name == null) {
            aib.setName(file.getFileName().toString());
        }
        MultiAlgorithmDSL dsl = new MultiAlgorithmDSL(loader, aib);
        try {
            LenskitConfigScript script = loader.loadScript(file.toFile());
            dsl.setBaseURI(script.getDelegate().getBaseURI());
            script.setDelegate(dsl);
            script.configure();
        } catch (IOException e) {
            throw new EvaluationException("cannot load configuration from " + file, e);
        }
        List<AlgorithmInstance> multi = dsl.getInstances();
        if (multi.isEmpty()) {
            multi = Collections.singletonList(aib.build());
        }
        logger.info("loaded {} algorithms from {}", multi.size(), file);
        return multi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Algorithm(")
          .append(getName())
          .append(")");

        Map<String, Object> subAttrs = new LinkedHashMap<>(attributes);
        subAttrs.remove("Algorithm");
        if (!subAttrs.isEmpty()) {
            sb.append("[");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(sb, subAttrs);
            sb.append("]");
        }
        return sb.toString();
    }
}
