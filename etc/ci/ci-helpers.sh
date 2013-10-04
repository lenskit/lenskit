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
        exec "$@"
    else
        if [ -z "$dir" ]; then
            echo + "$@"
            "$@"
            ec="$?"
        else
            echo "[in $dir]" "$@"
            (cd "$dir" && "$@")
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

# Check if this build is a master build (on grouplens/lenskit, jdk7, not pull request)
skip_unless_master_build()
{
    key="$1"
    if [ "$TRAVIS_JDK_VERSION" != "$DEPLOY_JDK" ]; then
        skip "$key disabled for JDK $TRAVIS_JDK_VERSION"
    fi

    if [ "$TRAVIS_PULL_REQUEST" != false ]; then
        skip "$key disabled for pull requests"
    fi

    if [ "$TRAVIS_REPO_SLUG" != "grouplens/lenskit" ]; then
        skip "$key disabled for forks"
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
