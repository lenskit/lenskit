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

import com.google.common.base.Supplier;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.config.BuilderCommand;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * An algorithm instance backed by an external program.
 */
@BuilderCommand(ExternalAlgorithmInstanceCommand.class)
public class ExternalAlgorithmInstance implements AlgorithmInstance {
    private final String name;
    private final Map<String, Object> attributes;
    private final List<String> command;

    public ExternalAlgorithmInstance(String name, Map<String,Object> attrs,
                                     List<String> cmd) {
        this.name = name;
        attributes = attrs;
        command = cmd;
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

    @Override
    public RecommenderInstance makeTestableRecommender(TTDataSet data, Supplier<SharedPreferenceSnapshot> snapshot) throws RecommenderBuildException {
        throw new UnsupportedOperationException("cannot use external recommenders");
    }
}
