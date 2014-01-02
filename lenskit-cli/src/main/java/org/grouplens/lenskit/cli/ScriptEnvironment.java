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
package org.grouplens.lenskit.cli;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;

/**
 * Class managing environments for running various scripts and classes.  Used by commands
 * to configure and handle their classpath and property arguments.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ScriptEnvironment {
    public static void configureArguments(Subparser parser) {
        parser.addArgument("-C", "--classpath")
              .dest("classpath")
              .type(URL.class)
              .action(Arguments.append())
              .setDefault()
              .metavar("URL")
              .help("add URL (jar or dir) to script classpath");
        parser.addArgument("-D", "--define")
              .dest("properties")
              .setDefault()
              .type(new PropertyDef())
              .action(Arguments.append())
              .metavar("PROP=VALUE")
              .help("set property PROP");
    }

    private static class PropertyDef implements ArgumentType<Pair<String,String>> {
        @Override
        public Pair<String, String> convert(ArgumentParser parser, Argument arg, String val) throws ArgumentParserException {
            int pos = val.indexOf('=');
            if (pos < 0) {
                throw new ArgumentParserException(val + " is not a property specification", parser);
            }
            return Pair.of(val.substring(0, pos), val.substring(pos + 1));
        }
    }

    private final Properties properties;
    private final List<URL> classpath;

    public ScriptEnvironment(Namespace ns) {
        properties = new Properties();
        for (Pair<String,String> arg: ns.<Pair<String,String>>getList("properties")) {
            properties.setProperty(arg.getKey(), arg.getValue());
        }

        classpath = ns.getList("classpath");
    }

    /**
     * Get the properties defined by the command line.
     * @return The command line properties.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get the classpath.
     * @return The classpath.
     */
    public List<URL> getClasspath() {
        return classpath;
    }

    /**
     * Get the class loader for this script environment.
     * @return The class loader.
     */
    public ClassLoader getClassLoader() {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = getClass().getClassLoader();
        }
        if (classpath.isEmpty()) {
            return parent;
        } else {
            URL[] urls = classpath.toArray(new URL[classpath.size()]);
            return new URLClassLoader(urls, parent);
        }
    }
}
