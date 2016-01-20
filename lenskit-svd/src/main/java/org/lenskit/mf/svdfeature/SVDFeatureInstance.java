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
package org.grouplens.lenskit.mf.svdfeature;

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstance {
    private double label;
    private ArrayList<Feature> gfeas;
    private ArrayList<Feature> ufeas;
    private ArrayList<Feature> ifeas;

    public SVDFeatureInstance() {
        gfeas = new ArrayList<Feature>();
        ufeas = new ArrayList<Feature>();
        ifeas = new ArrayList<Feature>();
    }

    public void addGlobalFea(Feature fea) {
        gfeas.add(fea);
    }

    public void addUserFea(Feature fea) {
        ufeas.add(fea);
    }

    public void addItemFea(Feature fea) {
        ifeas.add(fea);
    }

    public ArrayList<Feature> getGlobalFeas() {
        return gfeas;
    }

    public ArrayList<Feature> getUserFeas() {
        return ufeas;
    }

    public ArrayList<Feature> getItemFeas() {
        return ifeas;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public double getLabel() {
        return label;
    }
}
