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
package org.grouplens.lenskit.eval;


import com.google.common.base.Preconditions;
import org.grouplens.lenskit.eval.config.EvalConfig;

import javax.annotation.Nonnull;

/**
 * The abstract class of Command.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public abstract class AbstractCommand<T> implements Command<T> {
    protected String name;
    private EvalConfig config;

    public AbstractCommand() {
        this("unnamed");
    }

    public AbstractCommand(@Nonnull String name) {
        this.name = name;
    }

    public AbstractCommand<T> setConfig(@Nonnull EvalConfig cfg) {
        Preconditions.checkNotNull(cfg, "configuration cannot be null");
        config = cfg;
        return this;
    }

    @Nonnull
    public EvalConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("no configuration is specified");
        }
        return config;
    }

    public AbstractCommand<T> setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public abstract T call() throws CommandException;
}
