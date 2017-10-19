/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    private StaticDataSource runtimeData;
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

    public DataSetBuilder setRuntime(StaticDataSource ds) {
        runtimeData = ds;
        return this;
    }

    public DataSetBuilder setTest(StaticDataSource ds) {
        testData = ds;
        return this;
    }

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
        return new DataSet(getName(), trainingData, runtimeData, testData, isoGroup, attrs, entityTypes);
    }
}
