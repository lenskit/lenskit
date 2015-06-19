package org.lenskit.specs.eval;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import org.lenskit.specs.AbstractSpec;

@JsonSubTypes({
        @Type(value=PartitionMethodSpec.Holdout.class, name="holdout"),
        @Type(value=PartitionMethodSpec.HoldoutFraction.class, name="holdout-fraction"),
        @Type(value=PartitionMethodSpec.Retain.class, name="retain")
})
public class PartitionMethodSpec extends AbstractSpec {
    private String order = "random";

    public String getOrder() {
        return order;
    }

    /**
     * Set the order to be used for this partition.
     * @param order The order, one of 'random' or 'timestamp'.
     */
    public void setOrder(String order) {
        this.order = order.toLowerCase();
    }

    /**
     * Spec for holding out *N* items.
     */
    public static class Holdout extends PartitionMethodSpec {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    /**
     * Spec for holding out a fraction of items.
     */
    public static class HoldoutFraction extends PartitionMethodSpec {
        private double fraction;

        public double getFraction() {
            return fraction;
        }

        public void setFraction(double fraction) {
            this.fraction = fraction;
        }
    }

    /**
     * Spec for retaining a fixed number of items.
     */
    public static class Retain extends PartitionMethodSpec {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
