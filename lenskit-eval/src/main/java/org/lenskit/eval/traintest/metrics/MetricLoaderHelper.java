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
package org.lenskit.eval.traintest.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.VerifyException;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Helper for loading metrics from the classpath.
 */
public class MetricLoaderHelper {
    private static final Logger logger = LoggerFactory.getLogger(MetricLoaderHelper.class);
    private final ClassLoader loader;
    private final Properties propFiles;

    /**
     * Create a new loader helper.
     * @param cl The class loader.
     * @param baseName The base name for files to look for; will load from META-INF/lenskit/baseName.properteis.
     * @throws IOException If there is an error loading the properties.
     */
    public MetricLoaderHelper(ClassLoader cl, String baseName) throws IOException {
        loader = cl;
        propFiles = new Properties();
        Enumeration<URL> urls = loader.getResources("META-INF/lenskit/" + baseName + ".properties");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try (InputStream stream = url.openStream()) {
                propFiles.load(stream);
            }
        }
    }

    /**
     * Look up the class implementing a metric by name.
     * @param name The metric name.
     * @return The metric class.
     */
    @Nullable
    public Class<?> findClass(String name) {
        if (propFiles.containsKey(name.toLowerCase())) {
            String className = (String) propFiles.get(name.toLowerCase());
            logger.debug("resolving metric {} to class {}", name, className);
            try {
                return ClassUtils.getClass(loader, className);
            } catch (ClassNotFoundException e) {
                throw new VerifyException("class " + className + " not found", e);
            }
        } else {
            try {
                logger.debug("trying to look up metric {} as class", name);
                return ClassUtils.getClass(loader, name);
            } catch (ClassNotFoundException e) {
                logger.debug("no metric {} found", name);
                return null;
            }
        }
    }

    /**
     * Get the metric type name from a node.  If the node is a string, the string is returned; otherwise, the object's
     * `type` property is returned.
     * @param node The node.
     * @return The type name.
     */
    public String getMetricTypeName(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            return obj.get("type").asText();
        } else {
            return null;
        }
    }

    @Nullable
    public <T> T createMetric(Class<T> type, JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        String typeName = getMetricTypeName(node);
        if (typeName == null) {
            return null;
        }
        if (!node.isObject()) {
            node = JsonNodeFactory.instance.objectNode().set("type", node);
        }

        Class<?> metric = findClass(typeName);
        if (metric == null) {
            logger.warn("could not find metric {} for ", typeName, type);
            return null;
        }
        for (Constructor<?> ctor: metric.getConstructors()) {
            if (ctor.getAnnotation(JsonCreator.class) != null) {
                return type.cast(mapper.convertValue(node, metric));
            }
        }

        // ok, just instantiate
        try {
            return type.cast(metric.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new VerifyException("Cannot instantiate " + metric, e);
        }
    }

    @Nullable
    public <T> T createMetric(Class<T> type, String json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read JSON string");
        }
        return createMetric(type, node);
    }
}
