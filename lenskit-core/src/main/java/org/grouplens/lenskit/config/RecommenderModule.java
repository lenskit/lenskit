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
package org.grouplens.lenskit.config;


/**
 * Base class for recommender modules.  Provides access to the core component.
 * Extend this class to create a module configuring a complete recommender network.
 * For a module configuring a portion of the network, such as the common KNN
 * parameters, subclass {@link RecommenderModuleComponent} instead.
 * 
 * <p>LensKit recommender modules should be built compositionally.  To create
 * the base module for a new recommender, extend this module and create the
 * modules containing the various parameter sets you need as fields.  Install
 * them using {@link #install(com.google.inject.Module)} in your
 * {@link #configure()} implementation.
 *
 * <p>Base classes must call {@link #configure()} to install the core module; this
 * is done by installing a {@link RecommenderCoreModule}.
 *
 * @see RecommenderModuleComponent
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderModule extends RecommenderModuleComponent {
    public final RecommenderCoreModule core;

    protected RecommenderModule() {
        core = new RecommenderCoreModule();
    }

    @Override
    protected void configure() {
        install(core);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        core.setName(name);
    }

}
