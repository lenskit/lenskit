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
