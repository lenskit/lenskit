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
package org.lenskit.util;

import java.util.Deque;
import java.util.LinkedList;

/**
 * A dynamic variable, manging scoped changes of value.  These values are **not** thread-local.
 */
public class DynamicVariable<T> {
    private final Deque<T> stack;
    private final Deque<Scope> scopes;

    public DynamicVariable() {
        stack = new LinkedList<>();
        scopes = new LinkedList<>();
    }

    /**
     * Create a dynamic variable with an initial value.
     * @param init The initial value of the dynamic variable.
     */
    public DynamicVariable(T init) {
        this();
        stack.push(init);
    }

    /**
     * Assign a new value to this dynamic variable.
     * @param val The value to assign.
     * @return The scope of the assignment; closing the scope undoes the assignment.
     */
    public Scope assign(T val) {
        Scope scope = new Scope();
        scopes.push(scope);
        stack.push(val);
        return scope;
    }

    /**
     * Get the current value of this dynamic variable.
     * @return The current value in the variable.
     */
    public T get() {
        if (stack.isEmpty()) {
            return null;
        } else {
            return stack.peek();
        }
    }

    /**
     * Class encapsulating a dynamic variable scope.
     */
    public class Scope implements AutoCloseable {
        @Override
        public void close() {
            if (scopes.peekFirst() != this) {
                throw new IllegalStateException("scope closed out of order");
            }
            assert stack.size() >= scopes.size();
            scopes.pop();
            stack.pop();
        }
    }
}
