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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitExtension {
    private Repository repo

    /**
     * Invoke a block with an open Git repository.  The block will receive the
     * Repository object as its first parameter, and optionally a Git object as
     * its second.  This method takes care of closing the repository.
     */
    public <V> V withRepo(Closure<V> block) {
        def bld = new FileRepositoryBuilder()
        if (repo == null) {
            repo = bld.readEnvironment().findGitDir().build()
            try {
                invokeWithRepo(block)
            } finally {
                try {
                    repo.close()
                } finally {
                    repo = null
                }
            }
        } else {
            invokeWithRepo(block)
        }
    }

    private <V> V invokeWithRepo(Closure<V> block) {
        if (block.maximumNumberOfParameters > 1) {
            block.call(repo, new Git(repo))
        } else {
            block.call(repo)
        }
    }

    ObjectId getHeadRevision() {
        withRepo { Repository repo -> repo.resolve(Constants.HEAD) }
    }

    RevCommit getHeadCommit() {
        withRepo { Repository repo ->
            def walk = new RevWalk(repo)
            walk.parseCommit(headRevision)
        }
    }
}
