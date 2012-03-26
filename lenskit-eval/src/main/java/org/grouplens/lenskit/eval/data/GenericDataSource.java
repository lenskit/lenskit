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
package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractEvalTask;
import org.grouplens.lenskit.eval.EvalTask;

import java.util.Set;

/**
 * @author Michael Ekstrand
 */
public class GenericDataSource implements DataSource {
    private String name;
    private DAOFactory daoFactory;
    private PreferenceDomain domain;

    public GenericDataSource(String name, DAOFactory factory) {
        this(name, factory, null);
    }

    public GenericDataSource(String name, DAOFactory factory, PreferenceDomain dom) {
        this.name = name;
        daoFactory = factory;
        domain = dom;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public DAOFactory getDAOFactory() {
        return daoFactory;
    }

    @Override
    public long lastModified() {
        return 0;
    }



}
