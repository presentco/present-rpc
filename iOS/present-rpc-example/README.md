
# Present RPC iOS Client Example

##  Install the Protocol Buffer compiler 

*Note: the generated files are included in this repository so if you just want to try the code skip ahead to "Run the Example Server".*

Use Homebrew to install the `present-rpc` command used to render `.proto` files to Swift services.

`brew install presentco/utils/present-rpc`

This will automatically install the `protoc` and `swift-protobuf` packages.

## Generate the Client Code

From the project directory run the `build-protos.sh`, which executes the following commands:

Run `protoc` to generate the protocol buffer message types:

`protoc --swift_out=present-rpc-example/protos --proto_path=../../java/example/src/main/proto echo.proto`

Run `present-rpc-compiler` to generate the client service stubs:

`present-rpc --proto_path=../../java/example/src/main/proto --swift_out=present-rpc-example/protos`


## Run the Example Server

See `java/example/README.md` for instructions to run the local server with the example `EchoService`

`cd present-rpc/java/example`
`gradle appRun`

## Run the Example Client

Run the example application in the XCode simulator and look at the console for results.

