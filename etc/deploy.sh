#!/bin/sh

git config --global user.email cibot@lenskit.org
git config --global user.name "LensKit CI"
./gradlew ciPublish --stacktrace
