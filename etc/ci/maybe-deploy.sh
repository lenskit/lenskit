#!/bin/sh

DEPLOY_JDK=oraclejdk7

. etc/ci/ci-helpers.sh
skip_unless_master_build deploy

case "$TRAVIS_BRANCH" in
master|release/*) DO_RUN=yes;;
*) skip "deploy disabled for branch $TRAVIS_BRANCH";;
esac

if [ -z "$CI_DEPLOY_USER" -o -z "$CI_DEPLOY_PASSWORD" ]; then
    echo "Deploy credentials unavailable" >&2
    exit 2
fi

echo "Running Maven deploy"
cmd -e mvn --batch-mode --settings etc/ci/settings.xml deploy -DskipTests=true -Dinvoker.skip=true