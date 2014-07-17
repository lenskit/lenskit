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
package org.grouplens.lenskit.util;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Closer;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * Directory to look up where classes might be found.  It loads lists of classes from
 * <tt>META-INF/classes.lst</tt> and exposes them for use in resolving unresolved imports.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ClassDirectory {
    /**
     * Get a class directory for the current thread's class loader.
     * @return A class directory.
     */
    public static ClassDirectory forCurrentClassLoader() {
        return forClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Get a class directory for a specific class loader.
     * @param loader The class loader.
     * @return The class directory.
     */
    public static ClassDirectory forClassLoader(ClassLoader loader) {
        ImmutableSetMultimap.Builder<String,String> mapping = ImmutableSetMultimap.builder();
        try {
            Enumeration<URL> urls = loader.getResources("META-INF/classes.lst");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Closer closer = Closer.create();
                try {
                    InputStream stream = closer.register(url.openStream());
                    Reader rdr = closer.register(new InputStreamReader(stream, "UTF-8"));
                    BufferedReader buf = closer.register(new BufferedReader(rdr));
                    String line = buf.readLine();
                    while (line != null) {
                        int idx = line.lastIndexOf('.');
                        if (idx >= 0) {
                            String name = line.substring(idx + 1);
                            String pkg = line.substring(0, idx);
                            mapping.put(name, pkg);
                        }
                        line = buf.readLine();
                    }
                } catch (Throwable th) {
                    throw closer.rethrow(th);
                } finally {
                    closer.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading class lists", e);
        }

        return new ClassDirectory(mapping.build());
    }

    private final SetMultimap<String, String> directory;

    private ClassDirectory(SetMultimap<String,String> map) {
        directory = map;
    }

    /**
     * Get the packages that contain a class with the specified name.
     * @param name The name to look for.
     * @return The set of packages (empty if the name is unknown).
     */
    public Set<String> getPackages(String name) {
        return directory.get(name);
    }
}
