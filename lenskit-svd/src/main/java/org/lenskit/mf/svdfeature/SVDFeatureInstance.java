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

package org.lenskit.mf.svdfeature;

import org.apache.commons.lang3.StringUtils;
import org.lenskit.featurizer.Feature;
import org.lenskit.solver.LearningInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstance implements LearningInstance {
    double weight;
    double label;
    final List<Feature> gfeas;
    final List<Feature> ufeas;
    final List<Feature> ifeas;

    public SVDFeatureInstance() {
        gfeas = new ArrayList<>();
        ufeas = new ArrayList<>();
        ifeas = new ArrayList<>();
        weight = 1.0;
        label = 0.0;
    }

    public SVDFeatureInstance(List<Feature> gfeas, List<Feature> ufeas,
                              List<Feature> ifeas) {
        this.gfeas = gfeas;
        this.ufeas = ufeas;
        this.ifeas = ifeas;
        label = 0.0;
        weight = 1.0;
    }

    public String toString() {
        ArrayList<String> fields = new ArrayList<>(5 + (gfeas.size() + ufeas.size() + ifeas.size()) * 2);
        fields.add(Double.toString(weight));
        fields.add(Double.toString(label));
        fields.add(Integer.toString(gfeas.size()));
        fields.add(Integer.toString(ufeas.size()));
        fields.add(Integer.toString(ifeas.size()));
        for (Feature fea : gfeas) {
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        for (Feature fea : ufeas) {
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        for (Feature fea : ifeas) {
            fields.add(Integer.toString(fea.getIndex()));
            fields.add(Double.toString(fea.getValue()));
        }
        return StringUtils.join(fields, " ");
    }
}
