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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.UserDAO;
import org.grouplens.lenskit.data.dao.UserListUserDAO;
import org.grouplens.lenskit.data.source.DataSource;
import org.grouplens.lenskit.eval.data.traintest.QueryData;
import org.lenskit.specs.SpecUtils;
import org.lenskit.specs.eval.DataSetSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * A train-test data set.
 */
public class DataSet {
    @Nonnull
    private final String name;
    @Nonnull
    private final DataSource trainData;
    @Nullable
    private final DataSource queryData;
    @Nonnull
    private final DataSource testData;
    @Nonnull
    private final UUID group;
    private final Map<String, Object> attributes;

    /**
     * Create a new data set.
     * @param name The name.
     * @param train The training source.
     * @param query The query source (if any).
     * @param test The test data source.
     * @param grp The data set isolation group.
     * @param attrs The data set attributes.
     */
    public DataSet(@Nonnull String name,
                   @Nonnull DataSource train,
                   @Nullable DataSource query,
                   @Nonnull DataSource test,
                   @Nonnull UUID grp,
                   Map<String, Object> attrs) {
        Preconditions.checkNotNull(train, "no training data");
        Preconditions.checkNotNull(test, "no test data");
        this.name = name;
        trainData = train;
        queryData = query;
        testData = test;
        group = grp;
        if (attrs == null) {
            attributes = Collections.emptyMap();
        } else {
            attributes = ImmutableMap.copyOf(attrs);
        }
    }

    /**
     * Create a train-test data set from a specification.
     * @param spec The specification.
     * @return The train-test data set.
     */
    public static DataSet fromSpec(DataSetSpec spec) {
        DataSetBuilder bld = new DataSetBuilder();
        // TODO support query sets
        bld.setName(spec.getName())
           .setTest(SpecUtils.buildObject(DataSource.class, spec.getTestSource()))
           .setTrain(SpecUtils.buildObject(DataSource.class, spec.getTrainSource()));
        bld.getAttributes().putAll(spec.getAttributes());
        return bld.build();
    }

    /**
     * Get the data set name.
     *
     * @return A name for the data set. Used in the output file.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the data set attributes (used for identification in output).
     *
     * @return A key &rarr; value map of the attributes used to identify this data
     *         set. For example, a crossfold data set may include the source
     *         name and fold number.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the isolation group ID for this data set.  Data sets in the same group will be allowed to
     * run in parallel.  This is used to implement data set isolation.
     *
     * @return The group ID for this data set.
     */
    public UUID getIsolationGroup() {
        return group;
    }

    /**
     * Get the training data.
     *
     * @return A data source containing the test data.
     */
    @Nonnull
    public DataSource getTestData() {
        return testData;
    }

    /**
     * Get the training data.
     *
     * @return A data source containing the training data.
     */
    @Nonnull
    public DataSource getTrainingData() {
        return trainData;
    }

    /**
     * Get the query data.
     *
     * @return A data source containing the query data.
     */
    @Nullable
    public DataSource getQueryData() {
        return queryData;
    }

    /**
     * Configure LensKit to have the training data from this data source.
     *
     * @param config A configuration in which the training data for this data set should be
     *               configured.
     */
    public void configure(LenskitConfiguration config) {
        trainData.configure(config);
        config.bind(QueryData.class, UserDAO.class)
              .to(new UserListUserDAO(getTestData().getUserDAO().getUserIds()));
    }

    public DataSetSpec toSpec() {
        DataSetSpec spec = new DataSetSpec();
        spec.setName(name);
        spec.setTrainSource(trainData.toSpec());
        spec.setTestSource(testData.toSpec());
        spec.setAttributes(attributes);
        // TODO support query data
        return spec;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataSet(")
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

    /**
     * Create a new generic train-test data set builder.
     * @return The new builder.
     */
    public static DataSetBuilder newBuilder() {
        return new DataSetBuilder();
    }

    /**
     * Create a new generic train-test data set builder.
     * @param name The data set name.
     * @return The new builder.
     */
    public static DataSetBuilder newBuilder(String name) {
        return new DataSetBuilder(name);
    }

    /**
     * Create a new builder initialized with this data set's values.
     * @return A new builder initialized to make a copy of this data set definition.
     */
    public DataSetBuilder copyBuilder() {
        return copyBuilder(this);
    }

    /**
     * Create a new builder initialized with this data set's values.
     * @return A new builder initialized to make a copy of this data set definition.
     */
    public static DataSetBuilder copyBuilder(DataSet data) {
        DataSetBuilder builder = newBuilder(data.getName());
        builder.setTest(data.getTestData())
               .setQuery(data.getQueryData())
               .setTrain(data.getTrainingData())
               .setIsolationGroup(data.getIsolationGroup());
        for (Map.Entry<String,Object> attr: data.getAttributes().entrySet()) {
            builder.setAttribute(attr.getKey(), attr.getValue());
        }
        return builder;
    }
}
