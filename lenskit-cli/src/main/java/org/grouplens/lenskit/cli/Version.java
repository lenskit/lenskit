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

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name="version", help="show the LensKit version")
public class Version implements Command {
    private final Namespace options;

    public Version(Namespace options) {
        this.options = options;
    }

    @Override
    public void execute() throws Exception {
        System.out.format("LensKit version %s\n", lenskitVersion());
    }

    public static void configureArguments(ArgumentParser parser) {
        parser.description("Prints the LensKit version.");
    }

    public static String lenskitVersion() {
        Properties props = new Properties();
        InputStream stream = Version.class.getResourceAsStream("/META-INF/lenskit/version.properties");
        try {
            props.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("properties error", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return props.getProperty("lenskit.version");
    }
}
