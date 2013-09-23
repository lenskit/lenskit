cmd()
{
    mode=run
    case "$1" in
    -e) mode=exec; shift;;
    esac

    echo + "$@"
    if [ "$mode" = exec ]; then
        exec "$@"
    else
        "$@"
        if [ "$?" -ne 0 ]; then
            echo "$1: exited with code $?" >&2
            exit 2
        fi
    fi
}

skip()
{
    echo "$@" >&2
    exit 0
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
    echo "travis_fold:start:$1"
}

travis_end_section()
{
    if [ ! -z "$TRAVIS_SECTION" ]; then
        echo "travis_fold:end:$TRAVIS_SECTION"
        TRAVIS_SECTION=
    fi
}

trap travis_end_section 0 INT QUIT TERM
