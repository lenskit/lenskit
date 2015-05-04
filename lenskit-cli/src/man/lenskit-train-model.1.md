# lenskit-train-model

## Name

**lenskit train-model** - train a LensKit model and serialize it to disk.

## Synopsis

**lenskit** [GLOBAL OPTIONS] **train-model** [OPTIONS] *CONFIG*...

## Description

The `train-model` command loads a LensKit algorithm configuration, instantiates its shareable
components, and writes the resulting recommender engine to a file.  This file can then be loaded
into an application or one of the other LensKit commands to provide recommendations and predictions.

## Options

*CONFIG*
:   A LensKit algorithm configuration file, written in the LensKit algorithm DSL for Groovy.  If
    multiple configuration files are specified, they are used together, with configuration in later
    files taking precedence over earlier files.

--help
:   Show usage help.

-o *FILE*, --output-file *FILE*
:   Write the resulting recommender model to *FILE*.  If this option is not specified, the model
    will be written to `model.bin` in the current directory.  If *FILE* ends in `.gz`, the file will
    be gzip-compressed.  Compressed model files can be transparently read by LensKit, so this is
    usually a good idea.

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
