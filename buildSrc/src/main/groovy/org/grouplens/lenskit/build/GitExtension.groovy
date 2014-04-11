package org.grouplens.lenskit.build

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