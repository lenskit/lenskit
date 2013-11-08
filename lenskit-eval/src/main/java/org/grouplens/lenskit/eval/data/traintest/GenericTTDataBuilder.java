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
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.data.DataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for generic train-test data sets.
 *
 * @see GenericTTDataSet
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GenericTTDataBuilder implements Builder<GenericTTDataSet> {
    private String name;
    private DataSource trainingData;
    private DataSource testData;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private DataSource queryData;

    public GenericTTDataBuilder() {
        this(null);
    }

    public GenericTTDataBuilder(String name) {
        this.name = name;
    }

    /**
     * Set the data set's name.
     * @param n The data set's name.
     * @return The builder (for chaining)
     */
    public GenericTTDataBuilder setName(String n) {
        name = n;
        return this;
    }

    public GenericTTDataBuilder setTrain(DataSource ds) {
        trainingData = ds;
        return this;
    }

    public GenericTTDataBuilder setQuery(DataSource query) {
        queryData = query;
        return this;
    }

    public GenericTTDataBuilder setTest(DataSource ds) {
        testData = ds;
        return this;
    }

    public String getName() {
        if (name != null) {
            return name;
        } else {
            return trainingData.getName();
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public GenericTTDataBuilder setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public GenericTTDataSet build() {
        if (attributes.isEmpty()) {
            attributes.put("DataSet", getName());
        }
        Preconditions.checkNotNull(trainingData, "train data is Null");
        return new GenericTTDataSet(getName(), trainingData, queryData, testData, attributes);
    }
}
