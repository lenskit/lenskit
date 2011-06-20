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
package org.grouplens.lenskit.data;


/**
 * Rating implementation for mutation by 
 * {@link org.grouplens.lenskit.data.dao.ScannerRatingCursor ScannerRatingCursor}.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MutableRating implements Rating {
    private long uid;
    private long iid;
    private double value;
    private long timestamp;
    
    @Override
    public long getUserId() {
        return uid;
    }
    public void setUserId(long uid) {
    	this.uid = uid;
    }
    
    @Override
    public long getItemId() {
        return iid;
    }
    public void setItemId(long iid) {
    	this.iid = iid;
    }
    
    @Override
    public double getRating() {
        return value;
    }
    public void setRating(double v) {
    	value = v;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long ts) {
    	timestamp = ts;
    }
    
    @Override
    public Rating clone() {
        return new SimpleRating(uid, iid, value, timestamp);
    }
}