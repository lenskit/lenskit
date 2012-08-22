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

import org.grouplens.lenskit.eval.AbstractCommand;

import java.lang.annotation.*;

/**
 * Specify the command for the default type of this class/interface to which
 * it is applied. Used to build objects when the user doesn't specify the
 * particular command factory to use.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
// FIXME Make this apply to methods as well
@Target({ElementType.TYPE, ElementType.METHOD})
@SuppressWarnings("rawtypes")
public @interface BuilderCommand {
    Class<? extends AbstractCommand> value();
}
