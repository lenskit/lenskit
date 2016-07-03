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
package org.lenskit.data.dao.file;

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.Entity;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import java.io.IOException;
import java.util.Collection;

/**
 * Entity source backed by a collection.
 */
public class CollectionEntitySource implements EntitySource {
    private final ImmutableList<Entity> entities;

    public CollectionEntitySource(Collection<? extends Entity> es) {
        entities = ImmutableList.copyOf(es);
    }

    @Override
    public ObjectStream<Entity> openStream() throws IOException {
        return ObjectStreams.wrap(entities);
    }
}
