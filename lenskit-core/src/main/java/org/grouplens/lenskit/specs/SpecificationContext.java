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
package org.grouplens.lenskit.specs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.grouplens.grapht.util.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ServiceLoader;

public class SpecificationContext {
    private static final Logger logger = LoggerFactory.getLogger(SpecificationContext.class);
    private final ClassLoader classLoader;
    @Nullable
    private final URI baseURI;

    private SpecificationContext(@Nonnull ClassLoader loader, @Nullable URI base) {
        classLoader = loader;
        baseURI = base;
    }

    /**
     * Create a new context with a default class loader and the default base URI.
     *
     * @return A configuration context.
     * @see #create(java.lang.ClassLoader, URI)
     */
    public static SpecificationContext create() {
        return create(null);
    }

    /**
     * Create a new context using a default class loader.
     *
     * @param base The base URI for resolving relative paths.
     * @return A configuration context.
     */
    public static SpecificationContext create(URI base) {
        return create(ClassLoaders.inferDefault(SpecificationContext.class), base);
    }

    /**
     * Create a new context.
     *
     * @param loader The class loader.
     * @param base The base URI for resolving relative paths.  May be {@code null}.
     * @return A configuration context.
     */
    public static SpecificationContext create(ClassLoader loader, @Nullable URI base) {
        return new SpecificationContext(loader, base);
    }

    /**
     * Get the base URI for resolving relative paths.
     *
     * @return The base URI, or {@code null} if there is no base URI.
     */
    @Nullable
    public URI getBaseURI() {
        return baseURI;
    }

    /**
     * Get the base URI for resolving relative paths, or the URI of the current working directory if
     * no base URI has been configured.
     *
     * @return The base URI, the URI of the current working directory if no base URI has been
     * configured.
     */
    @Nonnull
    public URI getBaseOrCurrentURI() {
        if (baseURI == null) {
            return new File(".").getAbsoluteFile().toURI();
        } else {
            return baseURI;
        }
    }

    /**
     * Resolve a path to a URI.  The path is resolved against the base URI, if configured, or the
     * current working directory.
     *
     * @param path The path to resolve.
     * @return The resolved absolute URI.
     */
    public URI resolve(String path) {
        return getBaseOrCurrentURI().resolve(path);
    }

    /**
     * Resolve a path to a URL.  The path is resolved against the base URI, if configured, or the
     * current working directory.
     *
     * @param path The path to resolve.
     * @return The resolved absolute URL.
     * @throws SpecificationException if the resulting URI is not a valid URL.
     */
    public URL resolveURL(String path) throws SpecificationException {
        URI uri = getBaseOrCurrentURI().resolve(path);
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new SpecificationException("invalid URL " + uri, e);
        }
    }

    /**
     * Resolve a path to a file.  The path is resolved against the base URI, if configured, or the
     * current working directory, and the result converted to a file.
     *
     * @param path The path to resolve.
     * @return The resolved file.
     * @throws java.lang.IllegalArgumentException if resolving the path does not produce a file URI.
     */
    public File resolveFile(String path) throws SpecificationException {
        URI uri = resolve(path);
        if (!uri.getScheme().equals("file")) {
            throw new SpecificationException("Invalid file URI: " + uri);
        }

        return new File(uri.getPath());
    }

    /**
     * Relativize a file against the base URI, if present; if no base URI is provided, then the
     * file's absolute path is used.
     * @param file The file to relativize.
     * @return The relative path string.
     */
    public String relativize(File file) {
        URI fileURI = file.toURI();
        if (baseURI == null) {
            return fileURI.toString();
        } else {
            return baseURI.relativize(fileURI).toString();
        }
    }

    /**
     * Configure an object using a specification and a particular spec handler.
     * @param specClass The specification handler interface, defining the kind of object to
     *                  configure.  If it is a concrete instantiable class, then the class is used
     *                  directly; otherwise, it is looked up using the {@linkplain java.util.ServiceLoader service provider interface}
     *                  and the implementations are searched for one that handles the type specified
     *                  in the {@code type} key in the {@code config}.
     * @param config A Typesafe configuration for the object.
     * @param <T> The object type.
     * @return A configured object.
     */
    public <T> T buildWithHandler(Class<? extends SpecHandler<T>> specClass, Config config) throws SpecificationException {
        SpecHandler<T> cfg = null;
        try {
            Constructor<? extends SpecHandler<T>> ctor = specClass.getConstructor();
            if (Modifier.isPublic(ctor.getModifiers())) {
                logger.debug("instantiating {}", specClass);
                cfg = ctor.newInstance();
            }
        } catch (NoSuchMethodException e) {
            /* no ctor found, that's fine */
        } catch (InvocationTargetException e) {
            throw new SpecificationException("Could not instantiate handler " + specClass, e);
        } catch (InstantiationException e) {
            throw new SpecificationException("Could not instantiate handler " + specClass, e);
        } catch (IllegalAccessException e) {
            throw new SpecificationException("Could not instantiate handler " + specClass, e);
        }

        if (cfg == null && config.hasPath("type")) {
            String type = config.getString("type");
            logger.debug("searching for implementation of {} for type {}", specClass, type);
            ServiceLoader<? extends SpecHandler<T>> loader = ServiceLoader.load(specClass, classLoader);
            for (SpecHandler<T> sh: loader) {
                if (sh.handlesType(type)) {
                    logger.debug("found configurator {} for type {}", cfg, type);
                    if (cfg == null) {
                        cfg = sh;
                    } else {
                        logger.warn("found multiple configurators of type {}, ignoring {}", type, sh);
                    }
                }
            }
            if (cfg == null) {
                throw new SpecificationException("No configurator found for type " + type);
            }
        }

        if (cfg != null) {
            return cfg.buildFromSpec(this, config);
        } else {
            throw new SpecificationException("could not instantiate " + specClass + " or find appropriate subclass");
        }
    }

    /**
     * Configure an object using a specification.
     * @param type The type of object to build.  The class must be annotated with {@link SpecHandlerInterface}
     *                 to specify a spec handler, or have a public static method {@code fromSpec} that takes
     *             a specification and a {@link Config} and creates an object.
     * @param config A Typesafe configuration for the object.
     * @param <T> The object type.
     * @return A configured object.
     */
    public <T> T build(Class<T> type, Config config) throws SpecificationException {
        SpecHandlerInterface shi = type.getAnnotation(SpecHandlerInterface.class);
        if (shi != null) {
            return (T) buildWithHandler((Class) shi.value(), config);
        } else {
            try {
                Method m = type.getMethod("fromSpec", SpecificationContext.class, Config.class);
                return (T) m.invoke(null, type, config);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Cannot create type " + type, e);
            } catch (InvocationTargetException e) {
                throw new SpecificationException("Could not instantiate " + type, e);
            } catch (IllegalAccessException e) {
                throw new SpecificationException("Access error instantiating " + type, e);
            }
        }
    }

    /**
     * Configure an object using a configuration file at a URI.
     * @param cfgClass The configurator interface, defining the kind of object to configure.
     * @param uri The URI of a configuration file or resource.
     * @param <T> The object type.
     * @return A configured object.
     */
    public static <T> T buildWithHandler(Class<? extends SpecHandler<T>> cfgClass, URI uri) throws SpecificationException {
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid URL " + uri, e);
        }
        SpecificationContext ctx = SpecificationContext.create(ClassLoaders.inferDefault(cfgClass), uri);
        Config config = ConfigFactory.parseURL(url);
        return ctx.buildWithHandler(cfgClass, config);
    }

    /**
     * Configure an object using a configuration file at a URI.
     * @param type The configurator interface, defining the kind of object to configure.
     * @param uri The URI of a configuration file or resource.
     * @param <T> The object type.
     * @return A configured object.
     */
    public static <T> T build(Class<T> type, URI uri) throws SpecificationException {
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid URL " + uri, e);
        }
        SpecificationContext ctx = SpecificationContext.create(ClassLoaders.inferDefault(type), uri);
        Config config = ConfigFactory.parseURL(url);
        return ctx.build(type, config);
    }
}
