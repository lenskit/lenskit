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
