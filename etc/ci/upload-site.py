#!/usr/bin/env python

from __future__ import with_statement
import sys, os, os.path
import requests
import hmac, hashlib
import zipfile

if len(sys.argv) >= 3:
    branch=sys.argv[1]
    url=sys.argv[2]
else:
    branch='master'
    url=None

staging = 'target/staging'
zipfn = 'target/lenskit-site.zip'

key = os.getenv('UPLOAD_SECRET')
if key is None:
    print >>sys.stderr, "no $UPLOAD_SECRET"
    sys.exit(1)

sys.stderr.write("Packing up zip file")
ndirs = 0
nfiles = 1
with zipfile.ZipFile(zipfn, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
    for (dir, dirs, files) in os.walk(staging):
        adir = os.path.relpath(dir, staging)
        if adir == '.':
            adir = ''
        zf.write(dir, adir)
        ndirs += 1
        if ndirs % 10 == 0:
            sys.stderr.write('.')
        for f in files:
            fn = os.path.join(dir, f)
            afn = os.path.join(adir, f)
            zf.write(fn, afn)
            nfiles += 1
print >>sys.stderr, "\nzipped %d files in %d dirs" % (nfiles, ndirs)

checksum = hmac.new(key, digestmod=hashlib.sha1)
with open(zipfn, 'rb') as f:
    data = f.read(8192)
    while data:
        checksum.update(data)
        data = f.read(8192)

auth = checksum.hexdigest()
if url is None:
    print "not uploading, but hmac is", auth
    sys.exit(0)

result = requests.post(url, data={'branch': branch, 'hmac': auth},
                       files={'archive': open(zipfn, 'rb')})
if result.status_code == 200:
    print >>sys.stderr, "Uploaded %s successfully" % (zipfn,)
    print result.content
else:
    print >>sys.stderr, "Upload failed with code %s" % (result.status_code,)
    print result.content
    sys.exit(2)