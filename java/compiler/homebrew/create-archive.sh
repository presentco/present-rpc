#!/bin/sh
version="1.0.0"
archive="archives/present-rpc-${version}.zip" 

# Build the compiler jar.
(cd ..; gradle build)

# Create the archive.
rm -f $archive

# -j ignores paths and places the files at the top level
zip -j $archive ../build/libs/present-rpc-compiler.jar scripts/present-rpc.sh

