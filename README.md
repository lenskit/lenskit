# LensKit

[![Build Status](https://travis-ci.org/lenskit/lenskit.png?branch=release/2.2)](https://travis-ci.org/lenskit/lenskit)
[![Windows build status](https://ci.appveyor.com/api/projects/status/github/lenskit/lenskit?branch=release/2.2)](https://ci.appveyor.com/project/elehack/lenskit)

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

Some of the tests require the [Pandas][] library for Python; if you want to run all the tests (`./gradlew check`), install it with with your operating system's package manager (yum, apt-get, brew, etc.), or use `pip`:

    pip install --user pandas

[Gradle]: http://www.gradle.org
[Pandas]: http://pandas.pydata.org/

## Working with the Code

To work with the LensKit code, import the Gradle project into your IDE.
IntelliJ IDEA includes support for Gradle projects, and this support works well
for LensKit in version 13 and later.  Gradle plugins are available for other
IDEs such as Eclipse.

No particular setup is needed for IntelliJ, which is what most of the LensKit
developers use.

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

## Running the Tests

After you make changes, it's good to run the unit tests.  You can run many of
them from your IDE; run all tests in the `org.grouplens.lenskit` package (and
subpackages) across all modules.

To run the full test suite, including data-driven unit tests and integration
tests, use Gradle:

    $ ./gradlew check

## Copyright

LensKit is under the following copyright and license:

Copyright 2010-2014 [LensKit Contributors](CONTRIBUTORS.md).  
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

Copyright 2010-2014 LensKit Contributors

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
to work on, we recommend perusing the open tickets on our [Trac][wiki]
or asking on the [mailing list][].

We prefer to receive code submissions as GitHub pull requests.  To
do this:

1. Fork the LensKit repository (`grouplens/lenskit`) on GitHub
2. Push your changes to your fork
3. Submit a pull request via the GitHub web interface
   
When submitting a pull request via GitHub, you warrant that you
either own the code or have appropriate authority to submit it, and
license your changes under LensKit's copyright license (LGPLv2.1+ for
the main code, 3-clause BSD for the build scripts).
