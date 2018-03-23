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
package org.lenskit.build

import org.gradle.util.ConfigureUtil

import java.util.regex.Pattern

/**
 * Extension object providing Travis environment utilities.
 */
class TravisExtension {
    boolean isActive() {
        return System.getenv('CI') == 'true'
    }

    String getRepo() {
        return System.getenv('TRAVIS_REPO_SLUG')
    }

    String getBranch() {
        return System.getenv('TRAVIS_BRANCH')
    }

    String getPullRequest() {
        def pr = System.getenv('TRAVIS_PULL_REQUEST')
        return pr == 'false' ? null : pr
    }

    String getActiveJdk() {
        return System.getenv('TRAVIS_JDK_VERSION')
    }

    Integer getBuildNumber() {
        return System.getenv('TRAVIS_BUILD_NUMBER')?.toInteger()
    }

    boolean isReleaseBuild() {
        if (!isActive()) {
            return false
        }
        if (pullRequest != null) {
            return false
        }
        return branch =~ /^(master|release\/[0-9.]+(?:-[A-Z0-9.+]?))$/
    }

    boolean isMasterBuild() {
        return isReleaseBuild() && branch == 'master' && repo == 'lenskit/lenskit'
    }

    void call(Closure block) {
        ConfigureUtil.configure(block, this)
    }
}
