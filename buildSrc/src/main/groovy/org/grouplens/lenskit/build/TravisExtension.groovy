package org.grouplens.lenskit.build

import org.gradle.util.ConfigureUtil

import java.util.regex.Pattern

/**
 * Extension object providing Travis environment utilities.
 */
class TravisExtension {
    def masterJdk
    String masterRepo
    private Pattern releaseBranchPattern

    Pattern getReleaseBranchPattern() {
        return releaseBranchPattern
    }

    void setReleaseBranchPattern(Pattern pat) {
        releaseBranchPattern = pat
    }

    void setReleaseBranchPattern(String pat) {
        releaseBranchPattern = Pattern.compile(pat)
    }

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

    boolean isInMasterRepo() {
        return repo == masterRepo
    }

    boolean isOnReleaseBranch() {
        def b = branch
        return b =~ releaseBranchPattern
    }

    boolean isPublishingActive() {
        System.getenv('CI_PUBLISH') == 'true' || (inMasterRepo && onReleaseBranch && pullRequest == null && activeJdk == masterJdk)
    }

    void call(Closure block) {
        ConfigureUtil.configure(block, this)
    }
}
