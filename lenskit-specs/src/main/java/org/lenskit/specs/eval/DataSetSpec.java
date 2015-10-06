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
package org.lenskit.specs.eval;

import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.data.DataSourceSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Spec for a train-test data set.
 */
public class DataSetSpec extends AbstractSpec {
    private String name;
    private DataSourceSpec trainSource;
    private DataSourceSpec testSource;
    private Map<String,Object> attributes = new HashMap<>();

    /**
     * Get the data source name.
     * @return The data source name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the data source name.
     * @param name The data source name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the training data source.
     * @return The training data source.
     */
    public DataSourceSpec getTrainSource() {
        return trainSource;
    }

    /**
     * Set the training data source.
     * @param src The trainig data source.
     */
    public void setTrainSource(DataSourceSpec src) {
        this.trainSource = src;
    }

    /**
     * Get the test data source.
     * @return The test data source.
     */
    public DataSourceSpec getTestSource() {
        return testSource;
    }

    /**
     * Set the test data source.
     * @param src The test data source.
     */
    public void setTestSource(DataSourceSpec src) {
        this.testSource = src;
    }

    /**
     * Get the attribute map of this data source.
     * @return The data source's attributes.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Set the attribute map of this data source.
     * @param attributes The data source's attribute map.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Set an attribute for this data source.
     * @param name The attribute name.
     * @param value The attribute value
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
}
