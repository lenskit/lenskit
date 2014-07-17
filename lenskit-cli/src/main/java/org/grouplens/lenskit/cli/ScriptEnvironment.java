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
package org.grouplens.lenskit.cli;

import com.google.common.collect.Lists;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.config.ConfigurationLoader;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
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
    private static final Logger logger = LoggerFactory.getLogger(ScriptEnvironment.class);

    public static void configureArguments(ArgumentParser parser) {
        ArgumentGroup group = parser.addArgumentGroup("script environment")
                                    .description("Options for interpreting Groovy scripts.");
        group.addArgument("-C", "--classpath")
             .dest("classpath")
             .type(URI.class)
             .action(Arguments.append())
             .metavar("URL")
             .help("add URL (jar or dir) to script classpath");
        group.addArgument("-D", "--define")
             .dest("properties")
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
    private final List<URI> classpath;

    public ScriptEnvironment(Namespace ns) {
        properties = new Properties();
        List<Pair<String,String>> props = ns.getList("properties");
        if (props != null) {
            for (Pair<String,String> arg: props) {
                properties.setProperty(arg.getKey(), arg.getValue());
            }
        }

        List<URI> cp = ns.getList("classpath");
        if (cp != null) {
            classpath = cp;
        } else {
            classpath = Collections.emptyList();
        }
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
    public List<URI> getClasspath() {
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
            URL[] urls = new URL[classpath.size()];
            URI base = new File(".").toURI();
            int i = 0;
            for (URI uri: classpath) {
                try {
                    urls[i] = base.resolve(uri).toURL();
                    logger.info("added to classpath: {}", urls[i]);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid URL", e);
                }
                i += 1;
            }
            return new URLClassLoader(urls, parent);
        }
    }

    public List<LenskitConfiguration> loadConfigurations(List<File> files) throws IOException, RecommenderConfigurationException {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        ConfigurationLoader loader = new ConfigurationLoader(getClassLoader());
        // FIXME Make properties available

        List<LenskitConfiguration> configs = Lists.newArrayListWithCapacity(files.size());
        for (File file: files) {
            configs.add(loader.load(file));
        }

        return configs;
    }
}
