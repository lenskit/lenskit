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
package org.grouplens.lenskit.slopeone;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.slopeone.CoratingMatrix;
import org.junit.Test;


public class TestCoratingMatrix {

	
	@Test
	public void testPutGet1() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(2, 5, 4));
		rs.add(new SimpleRating(1, 3, 5));
		rs.add(new SimpleRating(2, 3, 4));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder builder = new PackedRatingSnapshot.Builder(manager.open());
		CoratingMatrix data = new CoratingMatrix(builder.build());
		data.put(5, 3, 2);
		assertEquals(2, data.get(5, 3));
		assertEquals(2, data.get(3, 5));	
		assertEquals(0, data.get(5, 5));
		assertEquals(0, data.get(3, 3));
	}
	
	@Test
	public void testPutGet2() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 4, 4));
		rs.add(new SimpleRating(2, 4, 5));
		rs.add(new SimpleRating(3, 4, 4));
		rs.add(new SimpleRating(1, 5, 3));
		rs.add(new SimpleRating(2, 5, 5));
		rs.add(new SimpleRating(3, 5, 1));
		rs.add(new SimpleRating(1, 6, 1));
		rs.add(new SimpleRating(2, 6, 5));
		rs.add(new SimpleRating(3, 6, 3));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder builder = new PackedRatingSnapshot.Builder(manager.open());
		CoratingMatrix data = new CoratingMatrix(builder.build());
		data.put(4, 5, 3);
		data.put(4, 6, 3);
		data.put(5, 6, 3);
		assertEquals(3, data.get(4, 5));
		assertEquals(3, data.get(5, 4));
		assertEquals(3, data.get(4, 6));
		assertEquals(3, data.get(6, 4));
		assertEquals(3, data.get(5, 6));
		assertEquals(3, data.get(6, 5));
	}
	
	@Test
	public void testPutGet3() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 6, 4));
		rs.add(new SimpleRating(2, 6, 2));
		rs.add(new SimpleRating(1, 7, 3));
		rs.add(new SimpleRating(2, 7, 2));
		rs.add(new SimpleRating(3, 7, 5));
		rs.add(new SimpleRating(4, 7, 2));
		rs.add(new SimpleRating(1, 8, 3));
		rs.add(new SimpleRating(2, 8, 4));
		rs.add(new SimpleRating(3, 8, 3));
		rs.add(new SimpleRating(4, 8, 2));
		rs.add(new SimpleRating(5, 8, 3));
		rs.add(new SimpleRating(6, 8, 2));
		rs.add(new SimpleRating(1, 9, 3));
		rs.add(new SimpleRating(3, 9, 3));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder builder = new PackedRatingSnapshot.Builder(manager.open());
		CoratingMatrix data = new CoratingMatrix(builder.build());
		data.put(6, 7, 2);
		data.put(6, 8, 2);
		data.put(6, 9, 1);
		data.put(7, 8, 4);
		data.put(7, 9, 2);
		data.put(8, 9, 2);
		assertEquals(2, data.get(6, 7));
		assertEquals(2, data.get(7, 6));
		assertEquals(2, data.get(6, 8));
		assertEquals(2, data.get(8, 6));
		assertEquals(1, data.get(6, 9));
		assertEquals(1, data.get(9, 6));
		assertEquals(4, data.get(7, 8));
		assertEquals(4, data.get(8, 7));
		assertEquals(2, data.get(7, 9));
		assertEquals(2, data.get(9, 7));
		assertEquals(2, data.get(8, 9));
		assertEquals(2, data.get(9, 8));
	}
	
	@Test
	public void testPutGet4() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 4, 3.5));
		rs.add(new SimpleRating(2, 4, 5));
		rs.add(new SimpleRating(3, 5, 4.25));
		rs.add(new SimpleRating(2, 6, 3));
		rs.add(new SimpleRating(1, 7, 4));
		rs.add(new SimpleRating(2, 7, 4));
		rs.add(new SimpleRating(3, 7, 1.5));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder builder = new PackedRatingSnapshot.Builder(manager.open());
		CoratingMatrix data = new CoratingMatrix(builder.build());
		data.put(4, 5, 0);
		data.put(4, 6, 1);
		data.put(4, 7, 2);
		data.put(5, 6, 0);
		data.put(5, 7, 1);
		data.put(6, 7, 1);
		assertEquals(0, data.get(4, 5));
		assertEquals(0, data.get(5, 4));
		assertEquals(1, data.get(4, 6));
		assertEquals(1, data.get(6, 4));
		assertEquals(2, data.get(4, 7));
		assertEquals(2, data.get(7, 4));
		assertEquals(0, data.get(5, 6));
		assertEquals(0, data.get(6, 5));
		assertEquals(1, data.get(5, 7));
		assertEquals(1, data.get(7, 5));
		assertEquals(1, data.get(6, 7));
		assertEquals(1, data.get(7, 6));
	}
}