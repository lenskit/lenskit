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
package org.grouplens.lenskit.eval;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean containing information about a data set.  Used to provide that information to the
 * algorithmInfo, if it needs it for some reason.  It is best avoided, but can be useful in certain
 * experimental setups.  The evaluators inject it into the configuration, so it is available
 * via DI.
 *
 * @since 1.1
 */
@Shareable
public class ExecutionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String algoName;
    private final Map<String, Object> algoAttributes;
    private final TTDataSet dataSet;

    private ExecutionInfo(String aName, Map<String, Object> aAttrs, TTDataSet data) {
        algoName = aName;
        algoAttributes = ImmutableMap.copyOf(aAttrs);
        dataSet = data;
    }

    /**
     * Get the algorithmInfo name.
     * @return The algorithmInfo name.
     */
    public String getAlgoName() {
        return algoName;
    }

    /**
     * Get the algorithmInfo attributes.
     * @return The algorithmInfo attributes.
     */
    public Map<String, Object> getAlgoAttributes() {
        return algoAttributes;
    }

    /**
     * Get the data set name.
     * @return The data set name.
     */
    public String getDataName() {
        return dataSet.getName();
    }

    /**
     * Get the data set attributes.
     * @return The data set attributes.
     */
    public Map<String, Object> getDataAttributes() {
        return dataSet.getAttributes();
    }

    /**
     * Get the active train-test data set.  Note that this method provides access to the test
     * data, enabling an algorithm to cheat.  <strong>Be careful not to cheat.</strong>
     * @return The active train-test data set.
     */
    public TTDataSet getDataSet() {
        return dataSet;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ExecutionInfo) {
            ExecutionInfo eo = (ExecutionInfo) o;
            EqualsBuilder eqb = new EqualsBuilder();
            return eqb.append(algoName, eo.algoName)
                      .append(algoAttributes, eo.algoAttributes)
                      .append(dataSet, eo.dataSet)
                      .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(algoName)
                  .append(algoAttributes)
                  .append(dataSet)
                  .hashCode();
    }

    /**
     * Create a new builder to build instances.
     * @return A new builder for execution info instances.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder for {@link ExecutionInfo} instances.
     */
    public static class Builder implements org.apache.commons.lang3.builder.Builder<ExecutionInfo> {
        private String algoName;
        private Map<String, Object> algoAttributes = new HashMap<String, Object>();
        private TTDataSet dataSet;

        @SuppressWarnings("deprecation")
        public Builder setAlgorithm(Attributed algorithm) {
            return setAlgoName(algorithm.getName()).setAlgoAttributes(algorithm.getAttributes());
        }

        @Deprecated
        public Builder setAlgoName(String algoName) {
            this.algoName = algoName;
            return this;
        }

        @Deprecated
        public Builder setAlgoAttributes(Map<String, Object> attrs) {
            algoAttributes = new HashMap<String,Object>(attrs);
            return this;
        }

        public Builder setDataSet(TTDataSet data) {
            dataSet = data;
            return this;
        }

        @Override
        public ExecutionInfo build() {
            return new ExecutionInfo(algoName, algoAttributes, dataSet);
        }
    }
}
