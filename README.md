# Present RPC

Simple, idiomatic, [Protocol Buffer-based](https://developers.google.com/protocol-buffers/docs/proto3) RPCs

## Features

* Simple! Post a JSON object, and receive a JSON object back.
* Clearly define APIs using Protocol Buffers.
* REST-friendly: Use your familiar tools.
* Optionally use Protocol Buffer binary encoding (instead of JSON). It's more efficient, and you can safely rename fields.
* Transport agnostic (currently supports 
HTTP, more to come!)
* Works with [App Engine](https://cloud.google.com/appengine/docs/java/)!

## Platforms

Present RPC has two layers:

1. Transport (like HTTP)
2. Library (client and/or server)

A library can theoretically support multiple transports so you can reuse your
client and server code.

We currently provide libraries for:

* [Java clients and servers](https://github.com/presentco/present-rpc/blob/master/java/README.md)
* [Javascript clients](https://github.com/presentco/present-rpc/blob/master/java/javascript-generator/README.md)
* [iOS (Swift) clients](https://github.com/presentco/present-rpc/blob/master/iOS/present-rpc-example/README.md)
* Or, you can just use JSON! See the entire spec below.
## By Example

Here's a Protocol Buffer definition for a service that echoes a value back to you:

```proto
service EchoService {
  rpc echo(EchoMessage) returns (EchoMessage);
}

message EchoMessage {
  uint32 value = 1;
}
```

You can call it with `curl` like so:

```bash
$ curl -i -H "Content-Type: application/json" -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```

That's it! [Here's a complete example](https://github.com/presentco/present-rpc/tree/master/java/example).

## HTTP Protocol Specification

* The client encodes the RPC argument and `POST`s it in the HTTP request body to `/[Service Name]/[Method Name]`.
* The client may specify an encoding in the `Content-Type` request header: 
  * JSON: `application/json`
  * Protocol Buffers: `application/x-protobuf`
  * JSON is the default. 
* The server responds with one of the following HTTP response codes:
  * `200` - Successful
  * `401` - Authentication required
  * `4XX` - A different client error
  * `5XX` - Server error
* If the RPC is successful, the server will encode the result and return it in the HTTP response body using the same encoding as the request.

## Why Protocol Buffers?

Some people like to use JSON alone, but JSON is a wire format, not an
API definition language. [Protocol Buffer](https://developers.google.com/protocol-buffers/docs/proto3) 
are a clean, simple, mature way to define APIs. You can generate client 
and server code and API documentation from Protocol Buffer definitions, 
saving effort, and ensuring everything stays in sync. You can still use JSON 
as the wire format, or you can use the more efficient Protocol Buffer binary 
format, which also enables you to change field names without breaking existing 
clients.

## What about gRPC?

gRPC depends on HTTP/2 which doesn't work everywhere (App Engine, Cloud Functions, 
etc.). [gRPC-Web](https://github.com/grpc/grpc-web) is complicated, non-idiotmatic,
and doesn't help here. Present RPC supports HTTP 1.x, it can benefit from
the performance improvements in HTTP/2, and its libraries can support
other transports entirely. Present RPC doesn't support streaming yet,
but it can in the future (please send a patch!).