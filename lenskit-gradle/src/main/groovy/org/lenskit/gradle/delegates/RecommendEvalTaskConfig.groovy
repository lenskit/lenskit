/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

    /**
     * The per-item output file for separated evaluations.
     */
    def itemOutputFile

    /**
     * Whether or not to treat items separately (Bellogin's 1R).
     */
    def boolean separateItems = false

    RecommendEvalTaskConfig(Project prj) {
        super(prj, 'recommend')
    }

    @Override
    Map getJson() {
        return super.getJson() + [list_size: listSize,
                                  candidates: candidates,
                                  exclude: exclude,
                                  label_prefix: labelPrefix,
                                  item_output_file: makeUrl(itemOutputFile),
                                  separate_items: separateItems]
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

    void separateItems(boolean sep) {
        separateItems = sep
    }

    void itemOutputFile(f) {
        itemOutputFile = f
    }
}
