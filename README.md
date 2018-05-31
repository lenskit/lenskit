# LensKit

[![Build Status](https://travis-ci.org/lenskit/lenskit.png?branch=master)](https://travis-ci.org/lenskit/lenskit)
[![Windows build status](https://ci.appveyor.com/api/projects/status/github/lenskit/lenskit?branch=master)](https://ci.appveyor.com/project/elehack/lenskit)
[![Test coverage](https://codecov.io/gh/lenskit/lenskit/branch/master/graph/badge.svg)](https://codecov.io/gh/lenskit/lenskit)
[![SonarQube test coverage](https://sonarcloud.io/api/project_badges/measure?project=lenskit&metric=coverage)](https://sonarcloud.io/dashboard?id=lenskit)
[![SonarQube technical debt](https://sonarcloud.io/api/project_badges/measure?project=lenskit&metric=sqale_index)](https://sonarcloud.io/dashboard?id=lenskit)
[![Coverity Scan Build Status](https://img.shields.io/coverity/scan/9190.svg)](https://scan.coverity.com/projects/lenskit-lenskit)
[![Join the chat at https://gitter.im/lenskit/lenskit](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lenskit/lenskit?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

LensKit is an implementation of collaborative filtering algorithms and
a set of tools for benchmarking them.  This readme is about working with
the LensKit source code.  For more information about
LensKit and its documentation, visit the [web site][] or [wiki][].  You 
can also find information on the [wiki][] about how to use LensKit 
without downloading the source code.  If this is your first time working
with LensKit we recommend checking out the [Getting Started][] guide.

[web site]: http://lenskit.org
[wiki]: http://github.com/lenskit/lenskit/wiki/
[Getting Started]: http://lenskit.org/documentation/basics/getting-started/
[mailing list]: https://groups.google.com/forum/#!forum/lenskit-recsys

LensKit is made available under the MIT license; see `LICENSE.md`.

## Installation and Dependency Management

LensKit is built and deployed with [Gradle][] and publishes its
artifacts to Maven Central.  To install it to the local Maven
repository, making it available to other projects using standard
Java-based tools, check out this repository and run `./gradlew
install`; it is then available to other projects by depending directly
on it in your Maven, Gradle, Ivy, or SBT project.  The source code can
also be checked out and used in most Java IDEs.

[Gradle]: http://gradle.org

## Working with the Code

To work with the LensKit code, import the Gradle project into your IDE.
Most modern Java IDEs include support for Gradle, including IntelliJ IDEA (used
by most LensKit developers), Eclipse, and NetBeans.

No other particular setup is needed.

## Modules

LensKit is comprised of several modules.  The top-level `lenskit`
module serves as a container to build them and provide common settings
and dependencies.  The other modules are as follows:

- `lenskit-api` -- the common, public recommender API exposed by LensKit, independent
  of its actual implementations.
- `lenskit-test` -- infrastructure and helper code for testing.
- `lenskit-core` -- the core support code and configuration facilities for
  the rest of LensKit. It is the entry point for most of what you want to do with
  LensKit, providing support for configuring and building recommenders.
- `lenskit-knn` -- k-NN recommenders (user-user and item-item collaborative
  filtering).
- `lenskit-svd` -- the FunkSVD recommender (and eventually real SVD recommenders).
- `lenskit-slopeone` -- Slope-One recommenders.
- `lenskit-eval` -- the evaluation framework and APIs, along with a command line
  evaluation runner.
- `lenskit-groovy` -- support for reading LensKit configurations from Groovy files.
- `lenskit-all` -- a metapackage you can depend on to pull in the rest of the LensKit packages.
- `lenskit-cli` -- the LensKit command line interface.
- `lenskit-gradle` -- the Gradle plugin to script the LensKit evaluator
- `lenskit-integration-tests` -- additional integration tests for LensKit.

## Running the Tests

After you make changes, it's good to run the unit tests.  You can run many of
them from your IDE; run all tests in the `org.grouplens.lenskit` package (and
subpackages) across all modules.

To run the full test suite, including data-driven unit tests and integration
tests, use Gradle:

    $ ./gradlew check

## Copyright

LensKit is under the following copyright and license:

Copyright 2014-2017 [LensKit Contributors](CONTRIBUTORS.md).
Copyright 2010-2014 Regents of the University of Minnesota
Work on LensKit has been funded by the National Science Foundation under
grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  
## Contributing to LensKit
  
We welcome contribution to LensKit.  If you are looking for something
to work on, we recommend perusing the open tickets on GitHub
or asking on the [mailing list][].

We prefer to receive code submissions as GitHub pull requests.  To
do this:

1. Fork the LensKit repository (`lenskit/lenskit`) on GitHub
2. Push your changes to your fork
3. Submit a pull request via the GitHub web interface

For additional details, including legal maters, please see [CONTRIBUTING.md](CONTRIBUTING.md).
