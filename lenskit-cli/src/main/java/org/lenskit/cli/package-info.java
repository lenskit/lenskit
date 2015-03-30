/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
/**
 * LensKit command line commands and infrastructure.
 * <p>
 * The LensKit command line interface is built around subcommands, the way the Git command-line
 * interface works.  Subcommands are implemented as instances of the {@link Command} interface. The
 * <tt>lenskit</tt> program searches for commands using the {@linkplain java.util.ServiceLoader Java
 * Service Loader} system, and makes all detected commands available.  This allows LensKit add-on
 * packages to define their own commands to be run by the LensKit command-line runner.
 * </p>
 * <p>
 * Argument parsing is handled by <a href="http://argparse4j.sourceforge.net/">argparse4j</a>.
 * </p>
 */
package org.lenskit.cli;
