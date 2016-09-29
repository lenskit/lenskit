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
package org.lenskit.bias;

/**
 * Interface for bias models that can be based on the user, item, or both.
 */
public interface BiasModel {
    /**
     * Get the global bias (intercept).
     * @return The global bias.
     */
    double getIntercept();

    /**
     * Get the user bias.
     * @param user The user ID.
     * @return The bias for the specified user, or 0 if the user's bias is unknown.
     */
    double getUserBias(long user);

    /**
     * Get the item bias.
     * @param item The item ID.
     * @return The bias for the specified ite, or 0 if the item's bias is unknown.
     */
    double getItemBias(long item);
}
