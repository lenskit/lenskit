/*
 * LensKit, a reference implementation of recommender algorithms.
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
/**
 * Configuration infrastructure for LensKit.
 * 
 * <p>LensKit uses <a href="http://code.google.com/p/google-guice">Guice</a> to
 * manage its configuration. The classes in this package provide a convenience
 * framework on top of Guice to manage LensKit configurations in a convenient way.
 * 
 * <p>LensKit modules are configured compositionally. To configure a recommender,
 * a top-level configuration module extending {@link RecommenderModule} is used.
 * This module delegates much of its configuration to component modules which
 * extend {@link RecommenderModuleComponent}. These commonly control shared
 * parameters, such as the base parameters common to all recommenders
 * ({@link RecommenderCoreModule}) and parameters common to families of recommenders.
 * 
 * <p>Recommender configurations also make heavy use of parameter annotations.
 * The core annotations are in the package {@link org.grouplens.lenskit.params}.
 * Parameter annotations are annotated with the {@link org.grouplens.lenskit.params.meta.Parameter}
 * annotation. {@link RecommenderModuleComponent} provides convenience faciliites
 * to automatically load default values for parameters. A future version of
 * LensKit may use an annotation processor to further provide type-checking of
 * configuration parameters.
 */
package org.grouplens.lenskit.config;