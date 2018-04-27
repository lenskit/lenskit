#!/bin/bash

set -e

JDK="$1"
if [ -z "$JDK" ]; then
    echo "No JDK requested" >&2
    exit 1
fi

echo "Looking for JDK $JDK"
jdk_url=
while read key url; do
    echo "found registered JDK $key at $url"
    if [ "$key" = "$JDK" ]; then
        jdk_url="$url"
    fi
done <etc/jdk-urls.txt
if [ -z "$jdk_url" ]; then
    echo "No URL found for $JDK" >&2
    exit 1
fi

tarfile=$(basename "$jdk_url")

mkdir -p "$HOME/java/dist"
cd "$HOME/java"
if [ ! -f "$tarfile" ]; then
    echo "Downloading $tarfile"
    wget -O "$tarfile.tmp" "$jdk_url"
    mv "$tarfile.tmp" "dist/$tarfile"
fi

tar xzf "dist/$tarfile"
if [ ! -d "$JDK" ]; then
    echo "Unpacking $tarfile did not create $JDK!" >&2
    exit 1
fi
echo "Importing system CA certificates"
for keyfile in /etc/ssl/certs/*.pem; do
    name=$(basename "$keyfile")
    "$JDK/bin/keytool" -keystore "$JDK/lib/security/cacerts" -storepass changeit \
        -importcert -file "$keyfile" -alias "SYS:$name" -noprompt
done