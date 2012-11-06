# LensKit-archetype-simple-analysis

This archetype is to build simple projects for doing analysis of
recommender algorithms.  The phases of analysis are:

* lenskit-pre-eval: retrieve a dataset
* lenskit-eval: prepare the dataset for analysis by creating cross-validation samples, 
  and run the algorithms on the dataset, producing evaluation summaries in csv files. 
* lenskit-post-eval: whatever else you want to do
* lenvkit-analyze: run R scripts against the results of the evaluation, producing 
  statistical analyses or graphics files.

This archetype is for projects that do all of these phases, but that put
all of the user files in the top-level directory.  This sort of
structure might be suitable for a student who want to use lenskit for
a class project, but who doesn't want to hunt around to see where all
the files go in a properly designed maven build.

The key user files are:

* get-data.xml: an ant script that fetches one of the datasets from MovieLens, and unzips it.  Run automatically by Maven when needed.
* eval-simple.groovy: a groovy script that runs against the fetched dataset performing simple evaluations of some recommender algorithms.
* chart.R: an R script that runs against the results of the evaluation to produce some simple charts.

## Building the Archetype

If you are working from the source tree, you can build the archetype
and install it into your local deployment with:

    mvn install

If you run the command from the top-level of the source tree it will
install all of LensKit, including this archetype.  If for some reason
you only need the archetype, it is much faster to run `mvn install`
from the archetype subdirectory.


## Creating a project using the archetype

To create a project using the archetype, change directories to the
directory where you want to create the new project.  (Hint: you will
usually NOT want to do this inside of the lenskit source tree.
Anywhere else is fine.)

Then create the project with:

    mvn archetype:generate \
      -DarchetypeGroupId=org.grouplens.lenskit \
      -DarchetypeArtifactId=lenskit-archetype-simple-analysis \
      -DarchetypeVersion=1.1-SNAPSHOT \
      -DinteractiveMode=no \
      -DgroupId=org.your-org \
      -DartifactId=your-directory-name \
      -Dversion=0.1

The first three definitions are the specification of the archetype you
wish to use to create the new project.  They should all be exactly as
typed above, except possibly for archetypeVersion, which should be the
current version of the archetype.  (Hint: this is the same as the
current LensKit version.)

The last three definitions are the specification of the new project
you are creating.  In general they can be anything you want.

Maven will run for a while, after which your new-directory-name will
exist, already populated with your new project!

## Running the project

You can run your project with:

    mvn lenskit-analyze -Dgrouplens-mldata-acknowledge=yes

This runs the mvn process through all of the phases of the lifecycle.
The resulting graphs will be the .pdf files in the analysis directory.

You can run those stages by hand if you prefer.  Running one stage
will run all earlier stages as well.

If you're tired of typing -Dgrouplens-mldata-acknowledge-yes each
time, you can uncomment the grouplens.mldata.acknowledgement property
in pom.xml, and change the value to "yes".  

## Next steps

The next steps are up to you!  You can explore LensKit evaluations by
experimenting with `eval-simple.groovy`, create new visualizations or
statistical analyses with improved versions of `chart.R`, or create
new recommender algorithms in the source tree below the `src`
directory, and compare them to the best-known recommender algorithms
that are already part of LensKit.  Enjoy!
