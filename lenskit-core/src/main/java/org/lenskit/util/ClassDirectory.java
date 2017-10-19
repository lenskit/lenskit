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
package org.lenskit.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ClassDirectory.class);

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
        Preconditions.checkNotNull(loader, "classLoader");
        ImmutableSetMultimap.Builder<String,String> mapping = ImmutableSetMultimap.builder();
        try {
            Enumeration<URL> urls = loader.getResources("META-INF/classes.lst");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream stream = url.openStream();
                     Reader rdr = new InputStreamReader(stream, Charsets.UTF_8);
                     BufferedReader buf = new BufferedReader(rdr)) {
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
                }
            }
        } catch (IOException e) {
            logger.warn("Could not load class lists", e);
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
