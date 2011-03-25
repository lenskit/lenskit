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
package org.grouplens.lenskit.eval.crossfold;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.BasicUserRatingProfile;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RandomUserRatingProfileSplitterTest {
	@Test
	public void testRandomSplit() {
		UserRatingProfileSplitter splitter = new RandomUserRatingProfileSplitter(0.333333);
		List<Rating> ratings = new ArrayList<Rating>();
		ratings.add(new SimpleRating(5, 2, 5));
		ratings.add(new SimpleRating(5, 5, 5));
		ratings.add(new SimpleRating(5, 3, 5));
		UserRatingProfile profile = new BasicUserRatingProfile(5, ratings);
		SplitUserRatingProfile split = splitter.splitProfile(profile);
		assertEquals(5, split.getUserId());
		assertEquals(2, split.getQueryVector().size());
		assertEquals(1, split.getProbeVector().size());
		assertEquals(10, split.getQueryVector().sum(), 1.0e-6);
		assertEquals(5, split.getProbeVector().sum(), 1.0e-6);
	}
}
