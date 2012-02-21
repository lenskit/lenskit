/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.traintest;

import org.grouplens.common.spi.ServiceProvider;
import org.grouplens.lenskit.eval.config.BuilderFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Eval provider for train-test evaluations. Name is “TrainTest”.
 * @author Michael Ekstrand
 */
@ServiceProvider
public class TrainTestBuilderFactory implements BuilderFactory<TTPredictEvaluation> {
    @Override
    public String getName() {
        return "trainTest";
    }

    @Override
    public TrainTestEvalBuilder newBuilder() {
        return new TrainTestEvalBuilder();
    }
}
