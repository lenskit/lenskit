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
    private final URI baseURI;

    private SpecificationContext(ClassLoader loader, URI base) {
        classLoader = loader;
        baseURI = base;
    }

    /**
     * Create a new context with a default class loader and the current working directory as the
     * base URI.
     *
     * @return A configuration context.
     */
    public static SpecificationContext create() {
        return create(new File(".").getAbsoluteFile().toURI());
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
     * @param base The base URI for resolving relative paths.
     * @return A configuration context.
     */
    public static SpecificationContext create(ClassLoader loader, URI base) {
        return new SpecificationContext(loader, base);
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

    public URI getBaseURI() {
        return baseURI;
    }
}
