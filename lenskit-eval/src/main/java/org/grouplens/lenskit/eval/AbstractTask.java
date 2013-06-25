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
package org.grouplens.lenskit.eval;


import com.google.common.base.Preconditions;
import org.grouplens.lenskit.eval.config.EvalConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class to simplify writing {@link EvalTask}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractTask<T> implements EvalTask<T> {
    @Nullable private String name;
    private EvalConfig config;

    /**
     * Initialize a command.
     * @param name The command's name.
     */
    protected AbstractTask(@Nullable String name) {
        this.name = name;
    }

    /**
     * Set the configuration in use for this command.  The evaluation framework automatically
     * calls this method, it is not necessary to call it manually.
     *
     * @param cfg The configuration.
     * @return The command (for chaining).
     */
    public AbstractTask<T> setEvalConfig(@Nonnull EvalConfig cfg) {
        Preconditions.checkNotNull(cfg, "configuration cannot be null");
        config = cfg;
        return this;
    }

    /**
     * Get the command's configuration.
     *
     * @return The command's configuration object.
     */
    @Nonnull
    public EvalConfig getEvalConfig() {
        if (config == null) {
            throw new IllegalStateException("no configuration is specified");
        }
        return config;
    }

    /**
     * Set this command's name.
     *
     * @param name The new name.
     * @return The command (for chaining).
     */
    public AbstractTask<T> setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns the {@link #name} field, throwing
     * {@link IllegalStateException} if it is {@code null}.  Commands should override it to provide
     * default derived names if they support such a concept.  Subclasses can call {@link #hasName()}
     * to query whether the name has been set.
     */
    @Override @Nonnull
    public String getName() {
        if (name == null) {
            throw new IllegalStateException("no name specified");
        } else {
            return name;
        }
    }

    /**
     * Query whether this command has a name specified.
     * @return {@code true} if a name has been set.
     */
    protected boolean hasName() {
        return name != null;
    }
}
