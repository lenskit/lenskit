#!/bin/sh

DEPLOY_JDK=oraclejdk7

. etc/ci/ci-helpers.sh
skip_unless_master_build site

case "$TRAVIS_BRANCH" in
master|release/*) DO_RUN=yes;;
*) skip "site disabled for branch $TRAVIS_BRANCH";;
esac

TARGET=$(echo "$TRAVIS_BRANCH" | sed -e 's@/@-@g')

echo "Building Maven site for $TARGET"
cmd mvn --batch-mode site-deploy -Dlenskit.web.path=/tmp/lenskit-web
cmd rsync -r /tmp/lenskit-web/ rsync://travis@elehack.net/lenskit-web/lenskit-$TARGET/