# LensKit

[![Build Status](https://travis-ci.org/lenskit/lenskit.png?branch=master)](https://travis-ci.org/lenskit/lenskit)
[![Windows build status](https://ci.appveyor.com/api/projects/status/github/lenskit/lenskit?branch=master)](https://ci.appveyor.com/project/elehack/lenskit)
[![Test coverage](https://codecov.io/gh/lenskit/lenskit/branch/master/graph/badge.svg)](https://codecov.io/gh/lenskit/lenskit)
[![SonarQube test coverage](https://sonarcloud.io/api/badges/measure?key=lenskit&metric=coverage)](https://sonarcloud.io/dashboard?id=lenskit)
[![SonarQube technical debt](https://sonarcloud.io/api/badges/measure?key=lenskit&metric=sqale_debt_ratio)](https://sonarcloud.io/dashboard?id=lenskit)
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

Copyright 2010-2016 [LensKit Contributors](CONTRIBUTORS.md).  
Work on LensKit has been funded by the National Science Foundation under
grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

### Build System Copyright

To ease reuse, the LensKit build system (all `.gradle` files and the contents
of the `buildSrc` directory) are licensed under the 3-clause BSD license:

Copyright 2010-2016 LensKit Contributors

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

- Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the
  distribution.

- Neither the name of the University of Minnesota nor the names of
  its contributors may be used to endorse or promote products derived
  from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  
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
