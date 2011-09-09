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

import java.util.NoSuchElementException;

/**
 * An extension of AbstractCursor that simplifies the mechanics of the
 * next()/hasNext() implementation to a simple method, poll().
 * 
 * @author Michael Ludwig
 * @param <T>
 */
public abstract class AbstractPollingCursor<T> extends AbstractCursor<T> {
    private boolean hasNextCalled;
    private T polled;
    
    public AbstractPollingCursor() {
        super();
    }
    
    public AbstractPollingCursor(int rowCount) {
        super(rowCount);
    }
    
    @Override
    public boolean hasNext() {
        if (!hasNextCalled) {
            polled = poll();
            hasNextCalled = true;
        }
        
        return polled != null;
    }

    @Override
    public T next() {
        if (!hasNextCalled)
            polled = poll();
        if (polled == null)
            throw new NoSuchElementException();
        
        T n = polled;
        polled = null;
        hasNextCalled = false;
        return n;
    }

    /**
     * Return the next element in this Cursor, or null if there are no more
     * elements. This must be safe to call multiple times at the end of its
     * collection.
     * 
     * @return The next element, or null
     */
    protected abstract T poll();
}
