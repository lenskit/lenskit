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
package org.lenskit.cli.util;

import com.google.common.collect.Lists;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.lang3.tuple.Pair;
import org.lenskit.config.ConfigurationLoader;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
    private final List<String> classpath;

    public ScriptEnvironment(Namespace ns) {
        properties = new Properties();
        List<Pair<String,String>> props = ns.getList("properties");
        if (props != null) {
            for (Pair<String,String> arg: props) {
                properties.setProperty(arg.getKey(), arg.getValue());
            }
        }

        List<String> cp = ns.getList("classpath");
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
    public List<String> getClasspath() {
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
            int i = 0;
            for (String path: classpath) {
                File file = new File(path);
                try {
                    urls[i] = file.toURI().toURL();
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
