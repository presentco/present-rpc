#!/bin/sh
# Build the example proto buffers and place the generated source into the project.
proto_path=../../java/example/src/main/proto 
protoc --swift_out=present-rpc-example/protos --proto_path=$proto_path echo.proto

