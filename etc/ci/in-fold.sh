#!/bin/sh

. etc/ci/ci-helpers.sh

section="$1"
shift
travis_begin_section "$section"
"$@"
ecode="$?"
travis_end_section
exit $ecode