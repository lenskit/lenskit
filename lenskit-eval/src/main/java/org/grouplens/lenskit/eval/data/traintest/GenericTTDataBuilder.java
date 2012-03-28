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
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.data.DataSource;
import org.kohsuke.MetaInfServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Michael Ekstrand
 */
public class GenericTTDataBuilder implements Builder<TTDataSet> {
    private String name;
    private DataSource trainingData;
    private DataSource testData;
    private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    public GenericTTDataBuilder() {
        this("unnamed");
    }

    public GenericTTDataBuilder(String name) {
        this.name = name;
    }

    public GenericTTDataBuilder setTrain(DataSource ds) {
        trainingData = ds;
        return this;
    }

    public GenericTTDataBuilder setTest(DataSource ds) {
        testData = ds;
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public GenericTTDataBuilder setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public GenericTTDataSet build() {
        if (attributes.isEmpty()) {
            attributes.put("DataSet", name);
        }
        return new GenericTTDataSet(name, trainingData,
                                    testData, trainingData.getPreferenceDomain(),
                                    attributes);
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
