/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.Builder;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.EntityType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for generic train-test data sets.
 *
 * @see DataSet
 */
public class DataSetBuilder implements Builder<DataSet> {
    private String name;
    private StaticDataSource trainingData;
    private StaticDataSource testData;
    private Map<String, Object> attributes = new LinkedHashMap<>();
    private UUID isoGroup = new UUID(0, 0);
    private List<EntityType> entityTypes = Lists.newArrayList(CommonTypes.RATING);

    public DataSetBuilder() {
        this(null);
    }

    public DataSetBuilder(String name) {
        setName(name);
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

    public DataSetBuilder setTrain(StaticDataSource ds) {
        trainingData = ds;
        return this;
    }

    public DataSetBuilder setTest(StaticDataSource ds) {
        testData = ds;
        return this;
    }

    // TODO set entity types
    public DataSetBuilder setEntityTypes(List<EntityType> typeList) {
        entityTypes = typeList;
        return this;
    }

    /**
     * Set the group ID for this data set.  The default is the all-0 UUID.
     * @param group The group ID, or `null` to reset to the default group.
     * @return The builder (for chaining).
     */
    public DataSetBuilder setIsolationGroup(@Nullable UUID group) {
        isoGroup = group != null ? group : new UUID(0,0);
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
        Preconditions.checkNotNull(trainingData, "train data is Null");
        Map<String, Object> attrs = attributes;
        if (!attrs.containsKey("DataSet")) {
            attrs = ImmutableMap.<String,Object>builder()
                                .put("DataSet", getName())
                                .putAll(attrs)
                                .build();
        }
        return new DataSet(getName(), trainingData, testData, isoGroup, attrs, entityTypes);
    }
}
