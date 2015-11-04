#!/bin/sh

git config user.email cibot@lenskit.org
git config user.name "LensKit CI"
./gradlew ciPublish --stacktrace
