#!/bin/sh

DEPLOY_JDK=oraclejdk7

skip()
{
    echo "$@" >&2
    exit 0
}

if [ "$TRAVIS_JDK_VERSION" != "$DEPLOY_JDK" ]; then
    skip "Site disabled for JDK $TRAVIS_JDK_VERSION"
fi

if [ "$TRAVIS_PULL_REQUEST" != false ]; then
    skip "Site disabled for pull requests"
fi

if [ "$TRAVIS_REPO_SLUG" != "grouplens/lenskit" ]; then
    skip "Site disabled for forks"
fi

case "$TRAVIS_BRANCH" in
master|release/*) DO_RUN=yes;;
*) skip "Site disabled for branch $TRAVIS_BRANCH";;
esac

TARGET=$(echo "$TRAVIS_BRANCH" | sed -e 's@/@-@g')

echo "Building Maven site for $TARGET"
mvn --batch-mode site-deploy -Dlenskit.web.path=/tmp/lenskit-web
rsync -r /tmp/lenskit-web rsync://travis@elehack.net/lenskit-web/lenskit-$TARGET