cmd()
{
    mode=run
    dir=
    case "$1" in
    -e) mode=exec; shift;;
    -d) dir="$2"; shift 2;;
    esac

    if [ "$mode" = exec ]; then
        echo "->" "$@"
        exec ./etc/ci/tslines.pl "$@"
    else
        if [ -z "$dir" ]; then
            echo + "$@"
            ./etc/ci/tslines.pl "$@"
            ec="$?"
        else
            echo "[in $dir]" "$@"
            old_pwd="$PWD"
            (cd "$dir" && "$old_pwd/ci/tslines.pl" "$@")
            ec="$?"
        fi
        if [ "$ec" -ne 0 ]; then
            echo "$1: exited with code $ec" >&2
            exit 2
        fi
    fi
}

skip()
{
    echo "$@" >&2
    if [ "$CI_FORCE_RUN" = true ]; then
        echo "but not, because run is forced" >&2
    else
        exit 0
    fi
}

is_main_jdk()
{
    if [ "$TRAVIS_JDK_VERSION" = "$DEPLOY_JDK" ]; then
        return 0
    else
        return 1
    fi
}

is_main_repo()
{
    if [ "$TRAVIS_REPO_SLUG" = "grouplens/lenskit" ]; then
        return 0
    else
        return 1
    fi
}

is_pr()
{
    case "$TRAVIS_PULL_REQUEST" in
    false) return 1;;
    *) return 0;;
    esac
}

is_release_branch()
{
    case "$TRAVIS_BRANCH" in
    master|release/*) return 0;;
    *) return 1;;
    esac
}

skip_if()
{
    "$1"
    if [ "$?" -eq 0 ]; then
        skip "$2 skipped: passed test $1"
    fi
}

skip_unless()
{
    "$1"
    if [ "$?" -ne 0 ]; then
        skip "$2 skipped: failed test $1"
    fi
}

TRAVIS_SECTION=

travis_begin_section()
{
    if [ ! -z "$TRAVIS_SECTION" ]; then
        echo "section already started" >&2
        exit 2
    fi
    TRAVIS_SECTION="$1"
    echo -n "travis_fold:start:$1\r"
}

travis_end_section()
{
    if [ ! -z "$TRAVIS_SECTION" ]; then
        echo "travis_fold:end:$TRAVIS_SECTION"
        TRAVIS_SECTION=
    fi
}

trap travis_end_section 0 INT QUIT TERM
