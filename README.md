# LensKit

[![Build Status](https://travis-ci.org/grouplens/lenskit.png?branch=master)](https://travis-ci.org/grouplens/lenskit)

LensKit is an implementation of collaborative filtering algorithms and
a set of tools for benchmarking them.  This readme is about working with
the LensKit source code.  For more information about
LensKit and its documentation, visit the [web site][] or [wiki][].  You 
can also find information on the [wiki][] about how to use LensKit 
without downloading the source code.  If this is your first time working
with LensKit we recommend checking out the [Getting Started][] guide.

[web site]: http://lenskit.grouplens.org
[wiki]: http://github.com/grouplens/lenskit/wiki/
[Getting Started]: http://github.com/grouplens/lenskit/wiki/GettingStarted
[mailing list]: https://wwws.cs.umn.edu/mm-cs/listinfo/lenskit

LensKit is made available under the GNU Lesser General Public License
(LGPL), version 2.1 or later.

## Installation and Dependency Management

LensKit is built and deployed with [Gradle][] and publishes its
artifacts to Maven Central.  To install it to the local Maven
repository, making it available to other projects using standard
Java-based tools, check out this repository and run `./gradlew
install`; it is then available to other projects by depending directly
on it in your Maven, Gradle, Ivy, or SBT project.  The source code can
also be checked out and used in most Java IDEs.

[Gradle]: http://www.gradle.org

## Modules

LensKit is comprised of several modules.  The top-level `lenskit`
module serves as a container to build them and provide common settings
and dependencies.  The other modules are as follows:

* `lenskit-api` -- the common, public recommender API exposed by LensKit, independent
  of its actual implementations.
* `lenskit-test` -- infrastructure and helper code for testing.
* `lenskit-data-structures` -- common data structures used by LensKit.
  These are split from `-core` so the API can depend on them.
* `lenskit-core` -- the core support code and configuration facilities for
  the rest of LensKit. It is the entry point for most of what you want to do with
  LensKit, providing support for configuring and building recommenders.
* `lenskit-knn` -- k-NN recommenders (user-user and item-item collaborative
  filtering).
* `lenskit-svd` -- the FunkSVD recommender (and eventually real SVD recommenders).
* `lenskit-slopeone` -- Slope-One recommenders.
* `lenskit-eval` -- the evaluation framework and APIs, along with a command line
  evaluation runner.
* `lenskit-all` -- a metapackage you can depend on to pull in the rest of the LensKit packages.
* `lenskit-cli` -- the LensKit command line interface.
* `lenskit-integration-tests` -- additional integration tests for LensKit.
  
## Contributing to LensKit
  
We welcome contribution to LensKit.  If you are looking for something
to work on, we recommend perusing the open tickets on our [Trac][wiki]
or asking on the [mailing list][].

We prefer to receive code submissions as GitHub pull requests.  To
do this:

1. Fork the LensKit repository (`grouplens/lenskit`) on GitHub
2. Push your changes to your fork
3. Submit a pull request via the GitHub web interface
   
When submitting a pull request via GitHub, you warrant that you
either own the code or have appropriate authority to submit it, and
license your changes under LensKit's copyright license (LGPLv2.1+).
