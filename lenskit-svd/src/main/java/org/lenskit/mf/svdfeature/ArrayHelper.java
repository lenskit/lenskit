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


/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ArrayHelper {

    public static double innerProduct(double[] base, double[] other) {
        double prod = 0;
        for (int i=0; i<base.length; i++) {
            prod += (base[i] * other[i]);
        }
        return prod;
    }

    public static void addition(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] += other[i];
        }
    }

    public static void subtraction(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] -= other[i];
        }
    }

    public static void copy(double[] base, double[] other) {
        for (int i=0; i<base.length; i++) {
            base[i] = other[i];
        }
    }

    public static void scale(double[] base, double x) {
        for (int i=0; i<base.length; i++) {
            base[i] *= x;
        }
    }

    public static void initialize(double[] base, double val) {
        for (int i=0; i<base.length; i++) {
            base[i] = val;
        }
    }

    public static void randomInitialize(double[] base) {
        for (int i=0; i<base.length; i++) {
            base[i] = Math.random()/10000;
        }
    }

    public static double squaredNorm(double[] base) {
        double normv = 0;
        for (int i=0; i<base.length; i++) {
            normv += base[i] * base[i];
        }
        return normv;
    }

    public static double norm(double[] base) {
        double normv = 0;
        for (int i=0; i<base.length; i++) {
            normv += base[i] * base[i];
        }
        return Math.sqrt(normv);
    }
}
