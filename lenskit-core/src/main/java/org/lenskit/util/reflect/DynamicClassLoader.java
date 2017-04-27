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

/**
 * Dynamic class loader for loading new classes.
 */
public class DynamicClassLoader extends ClassLoader {
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
        String name = def.name.replace('/', '.');
        return defineClass(name, bytes, 0, bytes.length);
    }
}
