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
package org.grouplens.lenskit.eval.data.traintest;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.data.DataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Michael Ekstrand
 */
public class GenericTTDataCommand extends AbstractCommand<GenericTTDataSet> {
    private DataSource trainingData;
    private DataSource testData;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public GenericTTDataCommand() {
        super("TTData");
    }

    public GenericTTDataCommand(String name) {
        this();
        if (name != null) {
            setName(name);
        }
    }

    public GenericTTDataCommand setTrain(DataSource ds) {
        trainingData = ds;
        return this;
    }

    public GenericTTDataCommand setTest(DataSource ds) {
        testData = ds;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public GenericTTDataCommand setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public GenericTTDataSet call() {
        if (attributes.isEmpty()) {
            attributes.put("DataSet", name);
        }
        Preconditions.checkNotNull(trainingData, "train data is Null");
        return new GenericTTDataSet(name, trainingData,
                                    testData, trainingData.getPreferenceDomain(),
                                    attributes);
    }
}
