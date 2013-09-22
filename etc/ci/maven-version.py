#!/usr/bin/env python

import xml.etree.ElementTree as etree

pom = etree.parse('pom.xml')
print pom.getroot().find('{http://maven.apache.org/POM/4.0.0}version').text
