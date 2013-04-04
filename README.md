# LensKit

LensKit is an implementation of collaborative filtering algorithms and
a set of tools for benchmarking them.  This readme is about working with
the LensKit source code.  For more information about
LensKit and its documentation, visit the [web site][] or [wiki][].  You 
can also find information on the [wiki][] about how to use LensKit 
without downloading the source code.  If this is your first time working
with LensKit we recommend checking out the [Getting Started][] guide.

[web site]: http://lenskit.grouplens.org
[wiki]: http://bitbucket.org/grouplens/lenskit/wiki/
[Getting Started]: http://bitbucket.org/grouplens/lenskit/wiki/GettingStarted
[mailing list]: https://wwws.cs.umn.edu/mm-cs/listinfo/lenskit

LensKit is made available under the GNU Lesser General Public License
(LGPL), version 2.1 or later.

## Installation and Dependency Management

LensKit is built and deployed via [Maven][].  To install it, check out
this repository and run `mvn install`; it is then available to other projects by
depending directly on it as above (consult `pom.xml` for the version to use; all LensKit
modules share the same version).  The source code can also be checked out and used
in most Java IDEs.  NetBeans and IntelliJ both include Maven support, LensKit should import
fine with no special tricks.  For Eclipse, we have a [page of instructions][UsingEclipse].

[Maven]: http://maven.apache.org
[UsingEclipse]: https://bitbucket.org/grouplens/lenskit/wiki/UsingEclipse

## Modules

LensKit is comprised of several modules.  The top-level `lenskit`
module serves as a container to build them and provide common settings
and dependencies.  The other modules are as follows:

* `lenskit-api` -- the common, public recommender API exposed by LensKit, independent
  of its actual implementations.
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
* `lenskit-eval-demo` -- a set of example scripts for configuring and running LensKit
  offline evaluations.
* `lenskit-package` -- a metapackage for preparing binary distributions, including
  scripts for running the evaluator.
* `lenskit-archetype-fancy-analysis` and `lenskit-archetype-simple-analysis` -- archetypes for creating user projects using LensKit.
  
## Contributing to LensKit
  
We welcome contribution to LensKit.  If you are looking for something
to work on, we recommend perusing the open tickets on our [Trac][wiki]
or asking on the [mailing list][].

We prefer to receive code submissions as BitBucket pull requests.  To
do this:

1. Fork the LensKit repository (`grouplens/lenskit`) on BitBucket
2. Push your changes to your fork
3. Submit a pull request via the BitBucket web interface
   
When submitting a pull request via BitBucket, you warrant that you
either own the code or have appropriate authority to submit it, and
license your changes under LensKit's copyright license (LGPLv2.1+).

## LensKit Archetypes

These archetypes are to build simple
(`lenskit-archetype-simple-analysis`) or more sophisticated
(`lenskit-archetype-fancy-analysis`) projects for doing analysis of
recommender algorithms.  Detailed information about using these
archetypes are in the src/main/resources/archetype-resources/Readme.md
file, which are installed in the top-level directory when a user uses
this archetype to generate a project.

* Be careful editing the pom in src/main/resources/archetype-resources, 
  because its variables are substituted
  at two different times.  Variables like ${project.version} are
  substituted at the time the archetype is run by a user to create a
  project.  Variables with backslashes in front of them like
  \${version} are left as variables by the archetype, so they can be
  substituted at project build time.
