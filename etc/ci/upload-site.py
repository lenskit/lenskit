#!/usr/bin/env python

from __future__ import with_statement
import sys, os
import requests
import hmac, hashlib

zipfile=sys.argv[1]
branch=sys.argv[2]
url=sys.argv[3]

key = os.getenv('UPLOAD_SECRET')
if key is None:
    print >>sys.stderr, "no $UPLOAD_SECRET"
    sys.exit(1)

checksum = hmac.new(key, digestmod=hashlib.sha1)
with open(zipfile, 'rb') as f:
    data = f.read(8192)
    while data:
        checksum.update(data)
        data = f.read(8192)

auth = checksum.hexdigest()
result = requests.post(url, data={'branch': branch, 'hmac': auth},
                       files={'archive': open(zipfile, 'rb')})
if result.status_code == 200:
    print >>sys.stderr, "Uploaded %s successfully" % (zipfile,)
    print result.content
else:
    print >>sys.stderr, "Upload failed with code %s" % (result.status_code,)
    print result.content
    sys.exit(2)