/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.core;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Object input stream that uses a custom class loader.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CustomClassLoaderObjectInputStream extends ObjectInputStream {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassLoaderObjectInputStream.class);
    private final ClassLoader classLoader;

    public CustomClassLoaderObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        classLoader = loader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        if (classLoader == null) {
            return super.resolveClass(desc);
        } else {
            String name = desc.getName();
            logger.debug("resolving class {}", name);
            return ClassUtils.getClass(classLoader, name);
        }
    }
}
