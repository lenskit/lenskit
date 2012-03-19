/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data.traintest;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractEvalTaskBuilder;
import org.grouplens.lenskit.eval.EvalTask;
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.GenericDataSource;
import org.kohsuke.MetaInfServices;

/**
 * @author Michael Ekstrand
 */
public class GenericTTDataBuilder extends AbstractEvalTaskBuilder implements Builder<TTDataSet> {
    private String name;
    private PreferenceDomain domain;
    private DataSource trainingData;
    private DataSource testData;

    public GenericTTDataBuilder() {
        this("unnamed");
    }

    public GenericTTDataBuilder(String name) {
        this.name = name;
    }

    public void setTrain(DataSource ds) {
        trainingData = ds;
    }

    public void setTest(DataSource ds) {
        testData = ds;
    }

    public GenericTTDataSet build() {
        // all DataSource are also EvalTask
        addDependency((EvalTask)trainingData);
        addDependency((EvalTask)testData);
        return new GenericTTDataSet(name, dependency, trainingData, testData, domain);
    }

    @MetaInfServices
    public static class Factory implements BuilderFactory<TTDataSet> {
        public String getName() {
            return "generic";
        }

        public GenericTTDataBuilder newBuilder(String name) {
            return new GenericTTDataBuilder(name);
        }
    }
}
