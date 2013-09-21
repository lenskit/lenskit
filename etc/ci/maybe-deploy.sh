#!/bin/sh

DEPLOY_JDK=oraclejdk7

skip()
{
    echo "$@" >&2
    exit 0
}

if [ "$TRAVIS_JDK_VERSION" != "$DEPLOY_JDK" ]; then
    skip "Deployment disabled for JDK $TRAVIS_JDK_VERSION"
fi

if [ "$TRAVIS_PULL_REQUEST" != false ]; then
    skip "Deployment disabled for pull requests"
fi

if [ "$TRAVIS_REPO_SLUG" != "grouplens/lenskit" ]; then
    skip "Deployment disabled for forks"
fi

case "$TRAVIS_BRANCH" in
master|release/*) DO_RUN=yes;;
*) skip "Deployment disabled for branch $TRAVIS_BRANCH";;
esac

if [ -z "$CI_DEPLOY_USER" -o -z "$CI_DEPLOY_USER" ]; then
    echo "Deploy credentials unavailable" >&2
    exit 2
fi

echo "Running Maven deploy"
exec mvn --batch-mode --settings etc/ci/settings.xml deploy -DskipTests=true -Dinvoker.skip=true