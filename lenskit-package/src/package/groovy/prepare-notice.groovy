/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
import java.util.zip.ZipFile

/* Prepare the NOTICE.txt file for the package. */

def pkgDir = new File(project.properties["packageDir"])
def libDir = new File(pkgDir, "lib")
def noticeFile = new File(pkgDir, "NOTICE.txt")

noticeFile.withPrintWriter { out ->
    out.println("This file contains the notices required by the libraries used by LensKit.")
    out.println()
    libDir.eachFileMatch ~/.*\.jar$/, { File jar ->
        def zip = new ZipFile(jar)
        try {
            def noticeEntry = zip.getEntry("META-INF/NOTICE.txt")
            if (noticeEntry != null) {
                def notice = zip.getInputStream(noticeEntry).text
                out.println("${jar.name}:")
                notice.eachLine { line ->
                    out.println("    ${line}")
                }
                out.println()
            }
        } finally {
            zip.close()
        }
    }
}