#!/bin/sh

DEPLOY_JDK=oraclejdk7

. etc/ci/ci-helpers.sh
skip_unless is_main_jdk site
skip_unless is_main_repo site
skip_if is_pr site
skip_unless is_release_branch site

UPLOAD_SITE=yes
if [ -z "$SITE_UPLOAD_URL" ]; then
    echo "No upload URL, skipping upload"
    UPLOAD_SITE=no
elif is_pr; then
    # because of skip, only here if we are forcing the run
    echo "Not uploading for pull request"
    UPLOAD_SITE=no
elif ! is_release_branch; then
    # because of skip, only here if we are forcing the run
    echo "Not uploading for non-release branch $TRAVIS_BRANCH"
    UPLOAD_SITE=no
fi

TARGET=$(echo "$TRAVIS_BRANCH" | sed -e 's@/@-@g')

echo "Building Maven site for $TARGET"
cmd -t mvn --batch-mode post-site site:stage -Dlenskit.web.url=http://dev.grouplens.org/lenskit/$TARGET
if [ "$UPLOAD_SITE" = yes ]; then
    cmd python etc/ci/upload-site.py "$TRAVIS_BRANCH" "$SITE_UPLOAD_URL"
else
    echo "No upload URL, skipping upload"
fi
