/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.gradle.delegates

import org.gradle.api.Project

/**
 * Eval task configuration for recommend tasks.  These have more parameters than predict tasks.
 */
class RecommendEvalTaskConfig extends EvalTaskConfig {
    /**
     * The list size.
     */
    def int listSize = -1

    /**
     * The candidate selector.
     */
    def String candidates

    /**
     * The exclude selector.
     */
    def String exclude

    /**
     * The label prefix (for column labels).
     */
    def String labelPrefix

    RecommendEvalTaskConfig(Project prj) {
        super(prj, 'recommend')
    }

    @Override
    Map getJson() {
        return super.getJson() + [listSize: listSize,
                                  candidates: candidates,
                                  exclude: exclude,
                                  label_prefix: labelPrefix]
    }

    void listSize(int sz) {
        listSize = sz
    }

    void candidates(String sel) {
        candidates = sel
    }

    void exclude(String sel) {
        exclude = sel
    }

    void candidateItems(String sel) {
        project.logger.warn('candidateItems is deprecated, use candidates')
        candidates = sel
    }

    void excludeItems(String sel) {
        project.logger.warn('excludeItems is deprecated, use exclude')
        exclude = sel
    }

    void labelPrefix(String pfx) {
        labelPrefix = pfx
    }
}
