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
package org.grouplens.lenskit.eval.algorithm;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to get a algorithm instances.
 *
 * @author Michael Ekstrand
 */
public class ExternalAlgorithmInstanceCommand extends AbstractCommand<ExternalAlgorithmInstance> {
    private Map<String, Object> attributes = new HashMap<String, Object>();
    private List<String> command;

    public ExternalAlgorithmInstanceCommand() {
        this("Unnamed");
    }

    public ExternalAlgorithmInstanceCommand(String name) {
        super(name);
    }

    /**
     * Set the algorithm name.
     *
     * @param n The name for this algorithm instance.
     * @return The command for chaining.
     */
    @Override
    public ExternalAlgorithmInstanceCommand setName(String n) {
        name = n;
        return this;
    }

    /**
     * Set an attribute for this algorithm instance. Used for distinguishing similar
     * instances in an algorithm family.
     *
     * @param attr  The attribute name.
     * @param value The attribute value.
     * @return The command for chaining.
     */
    public ExternalAlgorithmInstanceCommand setAttribute(@Nonnull String attr, @Nonnull Object value) {
        Preconditions.checkNotNull(attr, "attribute names cannot be null");
        Preconditions.checkNotNull(value, "attribute values cannot be null");
        attributes.put(attr, value);
        return this;
    }

    /**
     * Get the attributes of this algorithm instance.
     *
     * @return A map of user-defined attributes for this algorithm instance.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Set the command to run. In order to have access to the relevant files, the following
     * strings will be substituted in command arguments:
     *
     * <ul>
     *     <li><code>{OUTPUT}</code> &mdash; the output file name
     *     <li><code>{TRAIN_DATA}</code> &mdash; the training CSV file name
     *     <li><code>{TEST_DATA}</code> &mdash; the test data CSV file name
     * </ul>
     *
     * @param cmd The command to run (name and arguments).
     * @return The command (for chaining)
     */
    public ExternalAlgorithmInstanceCommand setCommand(List<String> cmd) {
        command = cmd;
        return this;
    }

    @Override
    public ExternalAlgorithmInstance call() throws CommandException {
        if (command == null) {
            throw new IllegalStateException("no command specified");
        }
        return new ExternalAlgorithmInstance(name, attributes, command);
    }

}
