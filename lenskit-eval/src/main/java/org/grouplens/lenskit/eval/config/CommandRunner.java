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

import groovy.lang.Closure;
import org.grouplens.lenskit.eval.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Invoke a configuration block.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
public interface CommandRunner {
    /**
     * Invoke a command, possibly using a configuration closure.
     * @param command The command to run.
     * @param closure The closure to configure it with.
     * @param <V> The return type of the command.
     * @return The results of the command's {@link Command#call()} method.
     */
    <V> V invoke(@Nonnull Command<V> command, @Nullable Closure<?> closure);
}
