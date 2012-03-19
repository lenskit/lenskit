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
package org.grouplens.lenskit.eval.config;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldBuilder;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factort to create builders of particular evaluator components.  Builder factories
 * are registered with the SPI and used to create builders which are then actually
 * used in configuration.
 *
 * @author Michael Ekstrand
 * @param <T> The type of objects built by builders from this factory.
 */
public interface BuilderFactory<T> {
    /**
     * Get the name of this factory, used to invoke it in configuration files.
     * @return The name by which builders created by this factory can be referenced
     * in configuration files.
     */
    String getName();

    /**
     * Create a new builder.
     *
     *
     * @param arg An argument passed when creating the builder.
     * @return A new builder to build objects of the type this factory is attached
     * to.
     */
    @Nonnull
    Builder<T> newBuilder(@Nullable String arg);
}
