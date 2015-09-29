package org.lenskit.eval.traintest.recommend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.lenskit.specs.AbstractSpec;

/**
 * Specification class for configuring precision/recall metrics.
 */
@JsonIgnoreProperties({"type"})
public class PRMetricSpec extends AbstractSpec {
    private String goodItems;
    private String suffix;

    public String getGoodItems() {
        return goodItems;
    }

    public void setGoodItems(String goodItems) {
        this.goodItems = goodItems;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
