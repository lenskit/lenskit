/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval;

import java.util.HashSet;
import java.util.Set;

/**
 * Dispatch evaluation requests to a set of listeners.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class EvalListenerManager implements EvaluationListener {
    private Set<EvaluationListener> listeners = new HashSet<EvaluationListener>();
    
    public void addListener(EvaluationListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(EvaluationListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void evaluationStarting() {
        for (EvaluationListener l: listeners) {
            l.evaluationStarting();
        }
    }

    @Override
    public void evaluationFinished(Exception err) {
        for (EvaluationListener l: listeners) {
            l.evaluationFinished(err);
        }
    }

    @Override
    public void jobGroupStarting(JobGroup group) {
        for (EvaluationListener l: listeners) {
            l.jobGroupStarting(group);
        }
    }

    @Override
    public void jobGroupFinished(JobGroup group) {
        for (EvaluationListener l: listeners) {
            l.jobGroupFinished(group);
        }

    }

    @Override
    public void jobStarting(Job job) {
        for (EvaluationListener l: listeners) {
            l.jobStarting(job);
        }
    }

    @Override
    public void jobFinished(Job job, Exception err) {
        for (EvaluationListener l: listeners) {
            l.jobFinished(job, err);
        }
    }

}
