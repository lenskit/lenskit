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

package org.grouplens.lenskit.data.vector;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.ThreadSafe;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * A data structure used to cache user rating <tt>SparseVectors</tt> inside of a
 * <tt>PackedRatingSnapshot</tt>. Concurrent reads to the cache are allowed, but
 * only one thread may write to the cache at a time.
 */
@ThreadSafe
public class VectorCache {
	
	private Long2ObjectMap<SparseVector> map;
	private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	private final Lock readLock = rwlock.readLock();
	private final Lock writeLock = rwlock.writeLock();
	
	public VectorCache(){
		map = new Long2ObjectOpenHashMap<SparseVector>();
	}
	
	
	/**
	 * Store a <tt>SparseVector</tt> in the cache.
	 * @param uid The user's ID
	 * @param ratings A <tt>SparseVector</tt> containing the user's ratings
	 */
	public void put(long uid, SparseVector ratings) {
		writeLock.lock();
		
		try {
			map.put(uid, ratings);
		}
		
		finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Retrieve a <tt>SparseVector</tt> from the cache.
	 * @param uid The user's ID
	 * @return A <tt>SparseVector</tt> containing the user's ratings.
	 */
	public SparseVector get(long uid) {
		readLock.lock();
		
		try {
			return map.get(uid);
		}
		
		finally {
			readLock.unlock();
		}
	}
}
