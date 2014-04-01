#!/bin/sh

DEPLOY_JDK=oraclejdk7

. etc/ci/ci-helpers.sh
skip_unless is_main_jdk deploy
skip_unless is_main_repo deploy
skip_if is_pr deploy
skip_unless is_release_branch deploy

if [ -z "$CI_DEPLOY_USER" -o -z "$CI_DEPLOY_PASSWORD" ]; then
    echo "Deploy credentials unavailable" >&2
    exit 2
fi

echo "Running Maven deploy"
cmd -t -e mvn --batch-mode --settings etc/ci/settings.xml deploy \
    -DskipTests=true -Dinvoker.skip=true \
    -DprepareUpload=true
