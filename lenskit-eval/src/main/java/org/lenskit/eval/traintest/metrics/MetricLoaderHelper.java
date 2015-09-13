package org.lenskit.eval.traintest.metrics;

import org.apache.commons.lang3.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Helper for loading metrics from the classpath.
 */
public class MetricLoaderHelper {
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
     * Try to instantiate a metric.
     * @param name The metric name (case-insensitive; will be looked up in lowercase).
     * @return The metric, or {@code null} if no such metric can be found.
     */
    public Object tryInstantiate(String name) {
        if (propFiles.containsKey(name.toLowerCase())) {
            String className = (String) propFiles.get(name.toLowerCase());
            Class<?> metric = null;
            try {
                metric = ClassUtils.getClass(loader, className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("class " + className + " not found", e);
            }
            try {
                return metric.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate " + metric, e);
            }
        } else {
            return null;
        }
    }
}
