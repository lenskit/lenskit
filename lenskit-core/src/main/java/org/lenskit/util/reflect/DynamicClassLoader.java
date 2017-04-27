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
package org.lenskit.util.reflect;

import org.grouplens.grapht.util.ClassLoaders;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Dynamic class loader for loading new classes.
 */
public class DynamicClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(DynamicClassLoader.class);
    private static final Path DEBUG_DIR;

    static {
        String prop = System.getProperty("lenskit.codegen.debugDir", null);
        if (prop == null) {
            DEBUG_DIR = null;
        } else {
            logger.info("emitting generated classes to {}", prop);
            DEBUG_DIR = Paths.get(prop);
            try {
                Files.createDirectories(DEBUG_DIR);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public DynamicClassLoader() {
        super(ClassLoaders.inferDefault());
    }

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> defineClass(ClassNode def) {
        ClassWriter cw = new ClassWriter(0);
        def.accept(cw);
        byte[] bytes = cw.toByteArray();
        if (DEBUG_DIR != null) {
            logger.debug("writing class {}", def.name);
            try {
                Path fn = DEBUG_DIR.resolve(def.name + ".class");
                Files.createDirectories(fn.getParent());
                Files.write(fn, bytes);
            } catch (IOException e) {
                logger.error("error writing class file " + def.name, e);
            }
        }
        String name = def.name.replace('/', '.');
        logger.debug("defining class {} (internal name {})", name, def.name);
        try {
            Class<?> cls = defineClass(name, bytes, 0, bytes.length);
            logger.debug("defined class {}", cls);
            return cls;
        } catch (ClassFormatError e) {
            throw new IllegalArgumentException("Format error in class " + name, e);
        }
    }
}
