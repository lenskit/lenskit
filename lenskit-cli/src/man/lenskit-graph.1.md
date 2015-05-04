# lenskit-graph

## Name

**lenskit graph** - produce a GraphViz diagram of a recommender configuration.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **graph** [OPTIONS] *CONFIGS*

## Description

The *graph* command loads a LensKit algorithm configuration from one or more configuration files
and produces a visualization of the resulting object graph.  This visualization is in GraphViz DOT
format, suitable for rendering with *dot*(1).  Visualizing recommender configurations is often
ueful for debugging configurations and making sure they produce the objects you expect.

## Options

*CONFIG*
:   A Groovy script containing a LensKit algorithm file in the LensKit configuration DSL.  If there
    are multiple configurations, the are passed in order to `LenskitRecommenderEngineBuilder`, so
    later configurations override earlier ones.

--help
:   Print usage help.

-o *FILE*, --output-file *FILE*
:   Write the GraphViz file to *FILE*.  The default output file is `recommender.dot`.

--domain *SPEC*
:   Use the preference domain *SPEC* as the preference domain in the configuration.  *SPEC* is of
    the form [*LOW*,*HIGH*]/*PREC*; the precision (and slash) can be omitted for continuously valued
    ratings.  As an example, ‘[0.5,5.0]/0.5’ will be a domain from 0.5 to 5.0 stars with a granularity
    of 1/2 star.

--model-file *FILE*
:   Load a pre-trained model from *FILE*.  In this mode, the configurations are applied as
    modifications to the model rather than used to build a graph from scratch.  The mdoel file can
    be compressed.

This command also takes the standard [script environment options](man:lenskit-script-environment(7)).

## See Also

[**lenskit**(1)](man:lenskit(1)), [**lenskit-script-environment**(7)](man:lenskit-script-environment(7))

## Project Information

This command is a part of LensKit, an open source recommender systems toolkit
developed by [GroupLens Research](http://grouplens.org).
Copyright 2010-2014 Regents of the University of Minnesota and contributors.

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
