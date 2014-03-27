#!/bin/sh

mvn --batch-mode install || exit 2
./etc/ci/test-archetypes.sh || exit 2
