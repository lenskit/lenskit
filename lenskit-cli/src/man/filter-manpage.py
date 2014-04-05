#!/usr/bin/env python

# LensKit, an open source recommender systems toolkit.
# Copyright 2010-2013 Regents of the University of Minnesota and contributors
# Work on LensKit has been funded by the National Science Foundation under
# grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51
# Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


# A Pandoc filter script to make a manpage standards-conformant but look sane in source.
# It does 3 things:
# - lift first H1 element (if it starts the document) to the title
# - lift all other headers by one level
# - upcase all L2 headers
#
# This script requires the 'pandocfilters' package (pip install pandocfilters)

import pandocfilters
from pandocfilters import walk, stringify, Header, Str
import json
import sys
import re
from collections import OrderedDict

MetaString = pandocfilters.elt('MetaString', 1)


def liftTitle(doc):
    "Lift the title from the document."
    meta = doc[0]
    content = doc[1]
    heading = None

    if content[0]['t'] == 'Header':
        if content[0]['c'][0] == 1:
            heading = content[0]

    if heading is None:
        print >> sys.stderr, 'warning: first block not a heading'
        sys.exit(1)

    title = stringify(heading['c'][2])
    meta['unMeta']['title'] = MetaString(title)
    return [meta, content[1:]]


def upcase(key, value, fmt, meta):
    if key == 'Str':
        return Str(value.upper())


def liftHeaders(key, value, fmt, meta):
    if key == 'Header':
        level, attrs, content = value
        level -= 1
        if level == 1:
            content = walk(content, upcase, fmt, meta)
        return Header(level, attrs, content)


_man_link_re = re.compile(r'^(#|(?:\./)?[^/]+\.\d\.html$)')


def dropLinks(key, value, fmt, meta):
    if key == 'Link':
        text, link = value
        url, title = link
        if _man_link_re.match(url) is not None:
            return text


doc = json.load(sys.stdin, object_pairs_hook=OrderedDict)
doc = liftTitle(doc)
doc = walk(doc, liftHeaders, 'man', doc[0]['unMeta'])
doc = walk(doc, dropLinks, 'man', doc[0]['unMeta'])

json.dump(doc, sys.stdout)
