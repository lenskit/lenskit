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
package org.grouplens.lenskit.eval.data.crossfold;

import java.util.Collections;
import java.util.List;

import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Events;

/**
 * Order an event sequence by timestamp.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <E>
 */
public class TimestampOrder<E extends Event> implements Order<E> {

	@Override
	public void apply(List<E> list) {
		Collections.sort(list, Events.TIMESTAMP_COMPARATOR);
	}

}
