/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelector;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to get a algorithmInfo instances.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExternalAlgorithmBuilder implements Builder<ExternalAlgorithm> {
    private String name;
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private File workDir = new File(".");
    private String outputDelimiter = ",";
    private List<String> command;

    public ExternalAlgorithmBuilder() {
        this("Unnamed");
    }

    public ExternalAlgorithmBuilder(String name) {
        this.name = name;
    }

    /**
     * Set the algorithmInfo name.
     *
     * @param n The name for this algorithmInfo instance.
     * @return The command for chaining.
     */
    public ExternalAlgorithmBuilder setName(String n) {
        name = n;
        return this;
    }

    /**
     * Get the algorithmInfo name.
     * @return The algorithmInfo's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set an attribute for this algorithmInfo instance. Used for distinguishing similar
     * instances in an algorithmInfo family.
     *
     * @param attr  The attribute name.
     * @param value The attribute value.
     * @return The command for chaining.
     */
    public ExternalAlgorithmBuilder setAttribute(@Nonnull String attr, @Nonnull Object value) {
        Preconditions.checkNotNull(attr, "attribute names cannot be null");
        Preconditions.checkNotNull(value, "attribute values cannot be null");
        attributes.put(attr, value);
        return this;
    }

    /**
     * Get the attributes of this algorithmInfo instance.
     *
     * @return A map of user-defined attributes for this algorithmInfo instance.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Set the command to run. In order to have access to the relevant files, the following
     * strings will be substituted in command arguments:
     *
     * <ul>
     *     <li><code>{OUTPUT}</code> &mdash; the output file name (should be delimited text).
     *     <li><code>{TRAIN_DATA}</code> &mdash; the training CSV file name
     *     <li><code>{TEST_DATA}</code> &mdash; the test data CSV file name
     * </ul>
     *
     * @param cmd The command to run (name and arguments).
     * @return The command (for chaining)
     */
    public ExternalAlgorithmBuilder setCommand(List<String> cmd) {
        command = cmd;
        return this;
    }

    public ExternalAlgorithmBuilder setCommand(String... cmd) {
        return setCommand(ImmutableList.copyOf(cmd));
    }

    /**
     * Set the working directory for the external recommender.
     * @param dir The working directory.
     * @return The working directory.
     */
    public ExternalAlgorithmBuilder setWorkDir(File dir) {
        workDir = dir;
        return this;
    }

    /**
     * Set the working directory for the external recommender.
     * @param dir The working directory.
     * @return The working directory.
     */
    public ExternalAlgorithmBuilder setWorkDir(String dir) {
        return setWorkDir(new File(dir));
    }

    /***
     * Set the delimiter of the recommender's output file.
     * @param delim The output delimiter.  The default delimiter is ','.
     * @return The input delimiter.
     */
    public ExternalAlgorithmBuilder setOutputDelimiter(String delim) {
        outputDelimiter = delim;
        return this;
    }

    @Override
    public ExternalAlgorithm build() {
        if (command == null) {
            throw new IllegalStateException("no command specified");
        }
        return new ExternalAlgorithm(getName(), attributes, command, workDir, outputDelimiter);
    }
}
