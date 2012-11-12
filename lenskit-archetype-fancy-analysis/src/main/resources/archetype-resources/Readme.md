# LensKit-archetype-fancy-analysis

This archetype is to build more sophisticated projects for doing
analysis of recommender algorithms.  The phases of recommendation, and
the actions that are associated with them in this archetype are:

* lenskit-pre-eval: usually retrieve a dataset
* lenskit-eval: create cross-validation samples, and run the algorithms on 
  the dataset, producing evaluation summaries in csv files. 
* lenskit-post-eval: no default actions; you can do whatever you want to
  do before analysis here.
* lenskit-analysis: run R scripts against the results of the evaluation, 
  producing statistical analyses or graphics files.

This archetype is for projects that do all three phases, and that
organize the input and output according to maven best practices.  All
of the scripts for the eval are in the src/eval directory.  Each
script takes in some input data, and produces some output data.  The
archetype is intended to be used with the following structure:

* lenskit-pre-eval: gets data into the target/data directory.  The 
  ant script in src/eval/get-data.xml is used to fetch the data.
* lenskit-eval: takes data from the target/data directory and creates
  a set of crossfold datasets also in target/data.  Runs an evaluation
  script in src/eval/eval.groovy, which operates on the crossfold data
  in target/data, and produces output in target/analysis.
* lenskit-analysis

The key user files that you are likely to want to edit are:
* pom.xml: to change the value of grouplens.mldata.acknowledgement, 
  or to change the dataset that is downloaded.
* src/eval/get-data.xml: to change the dataset that is downloaded.  
  May require changes in pom.xml as well.
* src/eval/eval.groovy: to change the lenskit evaluation that is run, 
  perhaps by configuring different recommenders.
* src/eval/chart.R: to change the analysis of the output data in target/analysis,  
  perhaps including the charts that are generated.

This structure fits the Maven model: all input files are in the src
tree, and all generated files are in the target tree, where they may
be cleaned by the clean target.

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
      -DarchetypeArtifactId=lenskit-archetype-fancy-analysis \
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

    mvn lenskit-analysis -Dgrouplens-mldata-acknowledge=yes

This runs the mvn process through the `analysis` stage of the
lifecycle, which comes after the three evaluation stages of the
project.

If you prefer, you can run the stages independently:

    mvn lenskit-pre-eval -Dgrouplens-mldata-acknowledge-yes

will fetch the dataset,

    mvn lenskit-eval -Dgrouplens-mldata-acknowledge-yes

will fetch the dataset and run the eval script,

    mvn lenskit-post-eval -Dgrouplens-mldata-acknowledge-yes

will fetch the dataset, run the eval script, and do any post eval
operations you wish.

The resulting graphs will be the .pdf files in the analysis directory.

If you're tired of typing -Dgrouplens-mldata-acknowledge-yes each
time, you can uncomment the grouplens.mldata.acknowledgement property
in pom.xml, and change the value to "yes".  

## Example Predictor

There is a simple example predictor in src/main/java.  This predictor includes a model that generates
item means, and a predictor based on that model.  You may find the model and predictor useful as 
starting points for your own predictors.

## Next steps

The next steps are up to you!  You can explore LensKit evaluations by
experimenting with `eval.groovy`, create new visualizations or
statistical analyses with improved versions of `chart.R`, or create
new recommender algorithms in the source tree below the `src`
directory, and compare them to the best-known recommender algorithms
that are already part of LensKit.  Enjoy!
