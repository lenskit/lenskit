#!/bin/bash

set -e

if [ -z "$custom_jdk" ]; then
    echo "No custom JDK requested" >&2
    exit
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
if [ ! -d "$custom_jdk" ]; then
    echo "Unpacking $tarfile did not create $custom_jdk!" >&2
    exit 1
fi
echo "Importing system CA certificates"
for keyfile in /etc/ssl/certs/Go*.pem; do
    "$custom_jdk/bin/keytool" -keystore "$custom_jdk/lib/security/cacerts" -storepass changeit \
        -importcert -file "$keyfile" -noprompt
done