#!/bin/sh

MLDATA_ZIP="$PWD/lenskit-integration-tests/target/data/ml100k.zip"
if [ ! -r "$MLDATA_ZIP" ]; then
    echo "MovieLens data not downloaded!" >&2
    exit 3
fi

cmd()
{
    echo + "$@"
    "$@"
    if [ "$?" -ne 0 ]; then
        echo "$1: exited with code $?" >&2
        exit 2
        fi
}

LENSKIT_VERSION=$(./etc/ci/maven-version.py)
echo "Testing archetypes for version $LENSKIT_VERSION"

cmd mkdir -p target/test-archetypes
cd target/test-archetypes || exit 1
TEST_ROOT="$PWD"

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
    cmd mvn -B -e -Dgrouplens.mldata.acknowledge=yes \
        -Dlenskit.eval.threadCount=2 \
        -Drscript.executable=Rscript \
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

generate simple-analysis
cmd cp "$MLDATA_ZIP" test-simple-analysis/ml100k.zip
execute simple-analysis
require_files test-simple-analysis/accuracy.pdf test-simple-analysis/speed.pdf

generate fancy-analysis
cmd mkdir -p test-fancy-analysis/target/data
cmd cp "$MLDATA_ZIP" test-fancy-analysis/target/data/ml100k.zip
execute fancy-analysis
require_files test-fancy-analysis/target/paper.pdf \
    test-fancy-analysis/target/analysis/speed.pdf \
    test-fancy-analysis/target/analysis/accuracy.pdf
