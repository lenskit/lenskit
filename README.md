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
      <version>1.0</version>
    </dependency>
    <dependency>
      <!-- to get the k-NN recommenders -->
      <groupId>org.grouplens.lenskit</groupId>
      <artifactId>lenskit-knn</artifactId>
      <version>1.0</version>
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

* `lenskit-api` -- the common, public recommender API exposed by LensKit, independent
  of its actual implementations.
* `lenskit-data-structures` -- common data structures used by LensKit.
  These are split from `-core` so the API can depend on them.
* `build-tools` -- configuration & support files for building LensKit
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


## Notes for Eclipse

If you want to work with the LensKit sources in Eclipse, the checkout is complicated a bit
by limitations in M2e's integration with Mercurial. You'll need the Maven support ([m2e][]),
[MercurialEclipse][HGE] plugin, and the Groovy development tools (at least to work with the
evaluator code).  To get the tools:

* m2e: Help / Install New Software.  m2e is in the default repository.  
* Mercurial: Download an installer from 
  http://mercurial.selenic.com/downloads/.  TortoiseHg works great for Windows.
* MercurialEclipse: Help / Install New Software.  Add the repository for MercurialEclipse 
  (http://mercurialeclipse.eclipselabs.org.codespot.com/hg.wiki/update_site/stable).  Tell MercurialEclipse 
  where to find your Mercurial executable in Window / Preferences / Team / Mercurial.
* Groovy: Help / Install New Software.  Add the repository for Groovy
  (http://dist.springsource.org/release/GRECLIPSE/e4.2/), and install the Groovy plugin
  and the m2e adapter.

 
Then do the following:

* Check out the LensKit source tree by selecting "Mercurial" / "Clone existing repository"
  from the Import dialog.
* Once the project is imported, right-click it and select "Configure" -> "Convert to Maven project"
  to activate Maven support on the LensKit parent project.
* Finally, right-click the `lenskit` project, choose "Import", and select "Maven" / "Existing
  Maven Projects". Choose all sub-projects of `lenskit` and import them.

The `lenskit-eval` project requires some additional attention to compile correctly in Eclipse:

* First, right-click the `lenskit-eval` project and select "Configure" -> "Convert to 
  Groovy project" to enable Groovy support.
* Next, right-click the `lenskit-eval` project and select "Properties" / "Java Build Path".
  Select the "Source" tab near the top of the window and click the "Link Source..." button on
  the left.
* In the window that appears, select the "Browse..." button and browse to the directory that
  contains the `lenskit` project. Then, select "lenskit-eval" / "src" / "main" / "groovy".
  You may change the suggested folder name if you wish, then click "finish".
* Finally, click the "Link Source..." button a second time and browse to "lenskit-eval" / 
  "src" / "test" / "groovy". Make sure that this folder has a different name than the first
  Groovy folder, and click "Finish" -> "OK".  
  
You will now have all components of LensKit imported and ready to edit and compile in Eclipse.

You can run Maven from Eclipse by right clicking on one of the pom.xml files, and selecting
"Build".  You can choose the goal you want to run, and it should directly run.  Note that you
will probably get the error message:

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.

while appears to be harmless.

Note that on Windows the LensKit logic has some 

[HGE]: http://javaforge.com/project/HGE
