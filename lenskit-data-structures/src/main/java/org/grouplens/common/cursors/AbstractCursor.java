/*
 * GroupLens Common Utilities
 * Copyright © 2011 Regents of the University of Minnesota
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of the University of Minnesota nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software has been partly funded by NSF grant IIS 08-08692.
 */
package org.grouplens.common.cursors;

import java.util.Iterator;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractCursor<T> implements Cursor<T> {
    private final int rowCount;
    
    public AbstractCursor() {
        this(-1);
    }
    
    public AbstractCursor(int rowCount) {
        this.rowCount = Math.max(rowCount, -1); // Just a convenience to make all neg. #s map to -1
    }
    
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	/**
	 * No-op implementation of the {@link Cursor#close()} method.
	 */
	@Override
	public void close() {
		// no-op
	}
	
	/**
	 * Implementation of {@link Cursor#fastNext()} that delegates to {@link #next()}.
	 */
	public T fastNext() {
	    return next();
	}
	
	public Iterable<T> fast() {
	    return new Iterable<T>() {
	        public Iterator<T> iterator() {
	            return new Iterator<T>() {
	                public boolean hasNext() {
	                    return AbstractCursor.this.hasNext();
	                }
	                public T next() {
	                    return fastNext();
	                }
	                public void remove() {
	                    throw new UnsupportedOperationException();
	                }
	            };
	        }
	    };
	}

	/**
	 * Get the iterator.  This method just returns <tt>this</tt>, so for-each
	 * loops can be used over cursors.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new CursorIterator<T>(this);
	}
}
