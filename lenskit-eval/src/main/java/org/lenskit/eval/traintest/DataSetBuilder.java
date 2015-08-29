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
package org.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.source.DataSource;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for generic train-test data sets.
 *
 * @see DataSet
 */
public class DataSetBuilder implements Builder<DataSet> {
    private String name;
    private DataSource trainingData;
    private DataSource testData;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private DataSource queryData;
    private UUID isoGroup = new UUID(0, 0);

    public DataSetBuilder() {
        this(null);
    }

    public DataSetBuilder(String name) {
        this.name = name;
    }

    /**
     * Set the data set's name.
     * @param n The data set's name.
     * @return The builder (for chaining)
     */
    public DataSetBuilder setName(String n) {
        name = n;
        return this;
    }

    public DataSetBuilder setTrain(DataSource ds) {
        trainingData = ds;
        return this;
    }

    public DataSetBuilder setQuery(DataSource query) {
        queryData = query;
        return this;
    }

    public DataSetBuilder setTest(DataSource ds) {
        testData = ds;
        return this;
    }

    /**
     * Set the group ID for this data set.  The default is the all-0 UUID.
     * @param group The group ID.
     * @return The builder (for chaining).
     */
    public DataSetBuilder setIsolationGroup(@Nonnull UUID group) {
        Preconditions.checkNotNull(group, "group ID");
        isoGroup = group;
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

    public DataSetBuilder setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public DataSet build() {
        if (attributes.isEmpty()) {
            attributes.put("DataSet", getName());
        }
        Preconditions.checkNotNull(trainingData, "train data is Null");
        return new DataSet(getName(), trainingData, queryData, testData, isoGroup, attributes);
    }
}
