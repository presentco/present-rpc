
# Present RPC iOS Client Example

##  Install the Protocol Buffer compiler

Note: the generated file is included in this repository so this step is unnecessary if you just want to try the code.

This installs the `protoc` command used to render `.proto` files to Swift source code.

`brew install swift-protobuf`

From the project directory run `protoc` to update the generated files:

`protoc --swift_out=present-rpc-example --proto_path=../../java/example/src/main/proto echo.proto`

or use the provided `build-protos.sh`


## Run the Example Server

See `java/example/README.md` for instructions to run the local server with the example `EchoService`

## Run the Example Client

Run the example application in the XCode simulator and look at the console for results.
