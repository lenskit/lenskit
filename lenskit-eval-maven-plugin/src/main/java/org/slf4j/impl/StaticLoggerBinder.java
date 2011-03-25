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
package org.slf4j.impl;

import org.grouplens.lenskit.eval.maven.MavenLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Logger binder for Maven logging.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
    public static String REQUESTED_API_VERSION = "1.6";
    
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    /* (non-Javadoc)
     * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactory()
     */
    @Override
    public ILoggerFactory getLoggerFactory() {
        return MavenLoggerFactory.getInstance();
    }

    /* (non-Javadoc)
     * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactoryClassStr()
     */
    @Override
    public String getLoggerFactoryClassStr() {
        return MavenLoggerFactory.class.getName();
    }

}
