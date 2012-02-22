package org.grouplens.lenskit.eval.data.traintest;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.config.BuilderFactory;
import org.grouplens.lenskit.eval.data.DataSource;

/**
 * @author Michael Ekstrand
 */
public class GenericTTDataBuilder implements Builder<TTDataSet> {
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
        return new GenericTTDataSet(name, trainingData, testData, domain);
    }

    @ServiceProvider
    public static class Factory implements BuilderFactory<TTDataSet> {
        public String getName() {
            return "generic";
        }

        public GenericTTDataBuilder newBuilder(String name) {
            return new GenericTTDataBuilder(name);
        }
    }
}
