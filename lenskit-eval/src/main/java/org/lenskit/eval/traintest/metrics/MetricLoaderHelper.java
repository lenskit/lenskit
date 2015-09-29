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
package org.lenskit.eval.traintest.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ClassUtils;
import org.lenskit.specs.SpecUtils;
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
    public Class<?> findClass(String name) {
        if (propFiles.containsKey(name.toLowerCase())) {
            String className = (String) propFiles.get(name.toLowerCase());
            logger.debug("resolving metric {} to class {}", name, className);
            try {
                return ClassUtils.getClass(loader, className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("class " + className + " not found", e);
            }
        } else {
            try {
                logger.debug("trying to look up metric {} as class", name);
                return ClassUtils.getClass(loader, name);
            } catch (ClassNotFoundException e) {
                logger.debug("no metric {} found");
                return null;
            }
        }
    }

    /**
     * Try to instantiate a metric.
     * @param name The metric name (case-insensitive; will be looked up in lowercase).
     * @return The metric, or {@code null} if no such metric can be found.
     */
    public Object tryInstantiate(String name) {
        Class<?> metric = findClass(name);
        if (metric != null) {
            try {
                return metric.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate " + metric, e);
            }
        } else {
            return null;
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
        String typeName = getMetricTypeName(node);
        if (typeName == null) {
            return null;
        }

        Class<?> metric = findClass(typeName);
        for (Constructor<?> ctor: metric.getConstructors()) {
            if (ctor.getAnnotation(JsonCreator.class) != null) {
                return type.cast(SpecUtils.createMapper().convertValue(node, metric));
            }
        }

        // ok, just instantiate
        try {
            return type.cast(metric.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot instantiate " + metric, e);
        }
    }
}
