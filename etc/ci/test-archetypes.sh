#!/bin/sh

. etc/ci/ci-helpers.sh

MLDATA_ZIP="$PWD/lenskit-integration-tests/target/data/ml100k.zip"
if [ ! -r "$MLDATA_ZIP" ]; then
    echo "MovieLens data not downloaded!" >&2
    exit 3
fi

LENSKIT_VERSION=$(./etc/ci/maven-version.py)
echo "Testing archetypes for version $LENSKIT_VERSION"

generate()
{
    cmd mvn -B archetype:generate \
        -DarchetypeGroupId="org.grouplens.lenskit" \
        -DarchetypeArtifactId="lenskit-archetype-$1" \
        -DarchetypeVersion="$LENSKIT_VERSION" \
        -DarchetypeCatalog="internal,local" \
        -DgroupId="org.grouplens.lenskit.it" \
        -DartifactId="test-$1" \
        -Dversion="1-SNAPSHOT"
}

execute()
{
    cd test-"$1" || exit 1
    cmd mvn -B -e -Dlenskit.eval.threadCount=2 \
        lenskit-publish
    cd "$TEST_ROOT" || exit 1
}

require_files()
{
    failed=no
    for file in "$@"; do
        if [ ! -e "$file" ]; then
            echo "$file: no such file or directory" >&2
            failed=yes
        fi
    done
    if [ "$failed" = yes ]; then
        echo "required files not found, failing" >&2
        exit 5
    fi
}

if git log --format=medium -n1 HEAD |fgrep -q "[skip archetypes]"; then
    skip "Skipping archetype tests"
fi

cmd mkdir -p target/test-archetypes
cd target/test-archetypes || exit 1
TEST_ROOT="$PWD"

travis_begin_section "archetype.simple"
generate simple-analysis
cmd cp "$MLDATA_ZIP" test-simple-analysis/ml100k.zip
execute simple-analysis
require_files test-simple-analysis/rmse.svg test-simple-analysis/ndcg.svg
require_files test-simple-analysis/build-time.svg test-simple-analysis/test-time.svg
travis_end_section

travis_begin_section "archetype.fancy"
generate fancy-analysis
cmd mkdir -p test-fancy-analysis/target/data
cmd cp "$MLDATA_ZIP" test-fancy-analysis/target/data/ml100k.zip
execute fancy-analysis
require_files test-simple-analysis/rmse.pdf test-simple-analysis/ndcg.pdf
require_files test-simple-analysis/build-time.pdf test-simple-analysis/test-time.pdf
travis_end_section
