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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Joiner;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.script.BuiltBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * An algorithmInfo instance backed by an external program.
 */
@BuiltBy(ExternalAlgorithmBuilder.class)
public class ExternalAlgorithm implements Attributed {
    private final Logger logger = LoggerFactory.getLogger(ExternalAlgorithm.class);

    private final String name;
    private final Map<String, Object> attributes;
    private final List<String> command;
    private final File workDir;
    private final String outputDelimiter;

    public ExternalAlgorithm(String name, Map<String, Object> attrs,
                             List<String> cmd, File dir, String delim) {
        this.name = name;
        attributes = attrs;
        command = cmd;
        workDir = dir;
        outputDelimiter = delim;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public List<String> getCommand() {
        return command;
    }

    public File getWorkDir() {
        return workDir;
    }

    public String getOutputDelimiter() {
        return outputDelimiter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExternalAlgorithm(")
          .append(getName())
          .append(")");
        if (!attributes.isEmpty()) {
            sb.append("[");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(sb, attributes);
            sb.append("]");
        }
        return sb.toString();
    }
}
