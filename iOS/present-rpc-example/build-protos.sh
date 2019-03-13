#!/bin/sh

# Path to the folder containing the example .proto schema files.
proto_path=../../java/example/src/main/proto 

# Destination for the generated source.
output_path=present-rpc-example/protos 

# Build the example protocol buffers types using protoc
echo "Build protocol buffers."
protoc --proto_path=$proto_path --swift_out=$output_path echo.proto

# Build the example client service stubs using present-rpc-compiler
echo "Build client service stubs."
java -jar ../../java/compiler/build/libs/present-rpc-compiler.jar --proto_path=$proto_path --swift_out=$output_path


