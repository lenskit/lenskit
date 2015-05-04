# lenskit-recommend

## Name

**lenskit recommend** - recommend items for users.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **global-recommend** [OPTIONS] *ITEM*...

## Description

The `global-recommend` command recommends items based on some reference items (e.g. a shopping
basket).  It loads a recommender from a trained model file and/or LensKit configuration scripts
and uses the configured algorithm to produce recommendations.

## Options

*ITEM*
:   One or more items to use as a reference.

--help
:   Show usage help.

-n *N*
:   Produce *N* recommendations.  The default is 10.

-m *FILE*, --model-file *FILE*
:   Load a trained recommender engine from *FILE*.

-c *SCRIPT*, --config-file *SCRIPT*
:   Configure the recommender using *SCRIPT*.  This option can be specified multiple times, and
    later configurations take precedence over earlier ones.  If `--model-file` is also specified,
    the scripts are used to modify the trained model.

--print-channel *CHAN*
:   In addition to item scores, also print the value in side channel *CHAN*.

This command also takes the standard [input data options](man:lenskit-input-data(7))
and [script environment options](man:lenskit-script-environment(7)).

## See Also

[**lenskit**(1)](man:lenskit(1)), [**lenskit-input-data**(7)](man:lenskit-input-data(7)),
[**lenskit-script-environment**(7)](man:lenskit-script-environment(7))

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
