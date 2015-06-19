package org.lenskit.specs.eval;

import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.data.DataSourceSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Spec for a train-test data set.
 */
public class TTDataSetSpec extends AbstractSpec {
    private String name;
    private DataSourceSpec trainSource;
    private DataSourceSpec testSource;
    private Map<String,Object> attributes = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceSpec getTrainSource() {
        return trainSource;
    }

    public void setTrainSource(DataSourceSpec trainSource) {
        this.trainSource = trainSource;
    }

    public DataSourceSpec getTestSource() {
        return testSource;
    }

    public void setTestSource(DataSourceSpec testSource) {
        this.testSource = testSource;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
