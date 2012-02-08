# LensKit

LensKit is an implementation of collaborative filtering algorithms and
a set of tools for benchmarking them.  For more information about
LensKit and its documentation, visit the [web site][] or [wiki][].

[web site]: http://lenskit.grouplens.org
[wiki]: http://dev.grouplens.org/trac/lenskit

If you just want to use LensKit, you don't need the source code; just use
Maven (or Ivy or any other Maven-compatible dependency manager) and declare
a dependency on LensKit:

    <dependency>
      <groupId>org.grouplens.lenskit</groupId>
      <artifactId>lenskit-core</artifactId>
      <version>0.9</version>
    </dependency>
    <dependency>
      <!-- to get the k-NN recommenders -->
      <groupId>org.grouplens.lenskit</groupId>
      <artifactId>lenskit-knn</artifactId>
      <version>0.9</version>
    </dependency>

## Installation and Dependency Management

LensKit is built and deployed via [Maven][].  To install it, check out
this repository and run `mvn install`; it is then available to other projects by
depending directly on it as above (consult `pom.xml` for the version to use; all LensKit
modules share the same version).  The source code can also be checked out and used
in most Java IDEs; NetBeans in IntelliJ both include Maven support, and the [m2e][]
plugin for Eclipse (installable from the Eclipse Marketplace) allows the project to
be opened in Eclipse.

[Maven]: http://maven.apache.org
[m2e]: http://eclipse.org/m2e

## Modules

LensKit is comprised of several modules.  The top-level `lenskit`
module serves as a container to build them and provide common settings
and dependencies.  The other modules are as follows:

* `lenskit-api` — the common, public recommender API exposed by LensKit, independent
  of its actual implementations.
* `lenskit-data-structures` — common data structures used by LensKit.
  These are split from `-core` so the API can depend on them.
* `lenskit-core` — the core support code and configuration facilities for
  the rest of LensKit. It is the entry point for most of what you want to do with
  LensKit, providing support for configuring and building recommenders.
* `lenskit-knn` — k-NN recommenders (user-user and item-item collaborative
  filtering).
* `lenskit-svd` — the FunkSVD recommender (and eventually real SVD recommenders).
* `lenskit-slopeone` — Slope-One recommenders.
* `lenskit-eval` — the evaluation framework and APIs, along with a command line
  evaluation runner.
* `lenskit-eval-demo` — a set of example scripts for configuring and running LensKit
  offline evaluations.
* `lenskit-package` — a metapackage for preparing binary distributions, including
  scripts for running the evaluator.


## Notes for Eclipse

If you want to work with the LensKit sources in Eclipse, the checkout is complicated a bit
by limitations in M2e's integration with Mercurial. You'll need the Maven support ([m2e][])
and the [MercurialEclipse][HGE] plugin. Then do the following:

* Check out the LensKit source tree by selecting “Mercurial” / “Clone existing repository”
  from the Import dialog.
* Once the project is imported, right-click it and select “Configure” → “Convert to Maven project”
  to activate Maven support on the LensKit parent project.
* Finally, right-click the `lenskit` project, choose “Import”, and select “Maven” / “Existing
  Maven Projects”. Choose all sub-projects of `lenskit` and import them.

You will now have all components of LensKit imported and ready to edit and compile in Eclipse.

[HGE]: http://javaforge.com/project/HGE
