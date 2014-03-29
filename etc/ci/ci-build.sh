#!/bin/sh

. ./etc/ci/ci-helpers.sh

echo "Starting CI build of LensKit"
echo "Building branch $TRAVIS_BRANCH of repository $TRAVIS_REPO_SLUG"
if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "Pull request $TRAVIS_PULL_REQUEST"
fi
test -n "$CI_DEPLOY_USER" && echo "have CI_DEPLOY_USER"
test -n "$CI_DEPLOY_PASSWORD" && echo "have CI_DEPLOY_PASSWORD"
test -n "$UPLOAD_SECRET" && echo "have UPLOAD_SECRET"

cmd -t mvn --batch-mode install
cmd -t ./etc/ci/test-archetypes.sh
