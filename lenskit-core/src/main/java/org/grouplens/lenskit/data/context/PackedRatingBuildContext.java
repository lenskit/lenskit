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
package org.grouplens.lenskit.data.context;

import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class PackedRatingBuildContext extends AbstractRatingBuildContext {
    protected final PackedRatingSnapshot snapshot;
    
    protected PackedRatingBuildContext(RatingDataAccessObject dao, PackedRatingSnapshot snapshot) {
        super(dao);
        this.snapshot = snapshot;
    }
    
    @Override
    public RatingSnapshot ratingSnapshot() {
        return snapshot;
    }

    @Override
    public RatingSnapshot trainingSnapshot() {
        // TODO implement training snapshots
        throw new UnsupportedOperationException();
    }

    @Override
    public RatingSnapshot tuningSnapshot() {
        // TODO implement tuning snapshots
        throw new UnsupportedOperationException();
    }
    
    public static RatingBuildContext make(RatingDataAccessObject dao) {
        return new PackedRatingBuildContext(dao, PackedRatingSnapshot.make(dao));
    }
}
