/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.script;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.util.AntBuilder;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Delegate to build a target.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.2
 */
public class TargetDelegate {
    private final AntBuilder ant;
    private Target target;

    public TargetDelegate(Target tgt) {
        target = tgt;
        ant = new LenskitAntBuilder(tgt.getProject(), tgt);
    }

    public void requires(Object... targets) {
        for (Object tgt : targets) {
            if (tgt instanceof Target) {
                target.addDependency(((Target) tgt).getName());
            } else {
                target.addDependency(tgt.toString());
            }

        }

    }

    public void perform(Closure<?> cl) {
        GroovyActionTask task = new GroovyActionTask(cl);
        task.setProject(target.getProject());
        target.addTask(task);
    }

    public final AntBuilder getAnt() {
        return ant;
    }

    /**
     * Invoke an Ant block.
     */
    public Task ant(Closure<?> block) {
        return (Task) getAnt().invokeMethod("sequential", block);
    }
}
