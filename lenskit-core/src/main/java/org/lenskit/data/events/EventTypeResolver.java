/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.events;

import org.apache.commons.lang3.ClassUtils;
import org.grouplens.grapht.util.ClassLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Look up event types.  This class looks for `META-INF/lenskit/event-builders.properties` files in the classpath and
 * looks up event type names in them.
 */
public class EventTypeResolver {
    private static final Logger logger = LoggerFactory.getLogger(EventTypeResolver.class);
    private final ClassLoader classLoader;
    private final Properties typeDefs;

    EventTypeResolver(ClassLoader loader) {
        classLoader = loader;
        typeDefs = new Properties();
        try {
            Enumeration<URL> files = classLoader.getResources("META-INF/lenskit/event-builders.properties");
            while (files.hasMoreElements()) {
                URL url = files.nextElement();
                try (InputStream str = url.openStream()) {
                    logger.debug("loading {}", url);
                    typeDefs.load(str);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("cannot scan for event type files", e);
        }
    }

    /**
     * Create a new event type resolver for a class loader.
     * @param loader The class loader.
     * @return The event type resolver.
     */
    public static EventTypeResolver create(ClassLoader loader) {
        return new EventTypeResolver(loader);
    }

    /**
     * Create a new event type resolver for the current class loader.
     * @return The event type resolver.
     * @see ClassLoaders#inferDefault()
     */
    public static EventTypeResolver create() {
        return new EventTypeResolver(ClassLoaders.inferDefault(EventTypeResolver.class));
    }

    /**
     * Get an event builder for the specified type name.  It first looks up the type name using the properties
     * files loaded from the classpath, then tries to instantiate it as a class.
     * @param name The type name.
     * @return The event builder.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public Class<? extends EventBuilder> getEventBuilder(String name) {
        String className = typeDefs.getProperty(name);
        if (className == null) {
            className = name;
        }

        try {
            return ClassUtils.getClass(classLoader, className).asSubclass(EventBuilder.class);
        } catch (ClassNotFoundException e) {
            logger.debug("cannot locate class {}", className);
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Class<? extends EventBuilder> getEventBuilder(Class<? extends Event> eventType) {
        BuiltBy bb = eventType.getAnnotation(BuiltBy.class);
        if (bb == null) {
            return null;
        }
        return bb.value();
    }
}
