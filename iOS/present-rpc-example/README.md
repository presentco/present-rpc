
# Present RPC iOS Client Example

##  Install the Protocol Buffer compiler 

*Note: the generated files are included in this repository so if you just want to try the code skip ahead to "Run the Example Server".*

Use Homebrew to install the `present-rpc` command used to render `.proto` files to Swift services.

`brew install presentco/utils/present-rpc`

This will automatically install the `protoc` and `swift-protobuf` packages.

## Generate the Client Code

From the project directory you can run the `build-protos.sh` to execute the following commands:

Generate the protocol buffer classes with `protoc`:

`protoc --swift_out=present-rpc-example/protos --proto_path=../../java/example/src/main/proto echo.proto`

These classes represent the argument and response types used with the service.  They provide methods to serialize and deserialize the data that is sent over the network using the protcol buffer format.  You can use these classes directly by posting the serialized data over HTTP to the service or you can use the client service stubs generated in the next steps to handle the network calls.

Generate the client service classes with `present-rpc`:

`present-rpc --proto_path=../../java/example/src/main/proto --swift_out=present-rpc-example/protos`

These classes provide client stubs for calling each method defined in the service.  These methods make the network call and return the results to you asynchronously.


## Run the Example Server

See `java/example/README.md` for instructions to run the local server with the example `EchoService`

`cd present-rpc/java/example`
`gradle appRun`

## Run the Example Client

Run the example application in the XCode simulator and look at the console for results.

