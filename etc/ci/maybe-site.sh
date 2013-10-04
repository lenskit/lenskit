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
cmd mvn --batch-mode site site:stage -Dlenskit.web.url=http://dev.grouplens.org/lenskit/$TARGET
if [ -n "$SITE_UPLOAD_URL" ]; then
    cmd python etc/ci/upload-site.py "$TRAVIS_BRANCH" "$SITE_UPLOAD_URL"
else
    echo "No upload URL, skipping upload"
fi