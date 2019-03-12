# Present RPC

Simple, idiomatic RPCs for Java, Javascript, Android, iOS, and more

## Features

* Simple! Post JSON, get JSON back.
* Clearly define APIs using [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/proto3).
* REST-friendly: Use your familiar tools.
* Supports Protocol Buffer binary encoding for improved performance and compatibility.
* Transport agnostic (currently supports 
HTTP, more to come!)
* Works with [App Engine](https://cloud.google.com/appengine/docs/java/)!

## Supported Platforms

Present RPC has two logical layers:

1. **Transport:** The underlying protocols, like [HTTP](#http-protocol-specification)
2. **Library:** Language-specific code for clients and servers

A library can theoretically support multiple transports, enabling you to reuse
client and server code.

We currently provide libraries for:

* [Java clients and servers](https://github.com/presentco/present-rpc/blob/master/java/README.md)
* [Javascript clients](#javascript-clients)
* [iOS (Swift) clients](https://github.com/presentco/present-rpc/blob/master/iOS/present-rpc-example/README.md)

You can also just roll your own client or server using JSON. See our [simple HTTP-based spec below](#http-protocol-specification).

## Example

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

## Javascript Clients

Use [`present-rpc-compiler`](https://github.com/presentco/present-rpc/blob/master/java/rpc-compiler/README.md)
to generate Javascript client libraries from Protocol Buffer definitions.

Running it against the [`echo.proto`](#example) example from above will generate 
[`echo.js`](), 
which we can use to invoke a remote `EchoService`:

```javascript
var service = new EchoService('http://localhost:8080');
service.echo({value: 42}).then(function(result) {
  alert('The answer is ' + result.value + '.');
});
```

As you can see, the generated functions return [`Promise`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise).
Set request headers before RPC calls like so:

```javascript
service.headers = {
  'Name', 'Value'
};
```

Use `catch()` to handle errors:

```javascript
service.echo(...)
  .then(...)
  .catch(function(e) {
    // e.code = HTTP status code
    // e.message = HTTP status message
    // e.xhr = The underlying XMLHttpRequest
  });

```

That's it! [Here's a complete example](https://github.com/presentco/present-rpc/tree/master/java/example).

## HTTP Protocol Specification

* The client encodes the RPC argument and `POST`s it in the HTTP request body to `/[Service Name]/[Method Name]`.
* The client may specify an encoding in the `Content-Type` request header: 
  * JSON (required): `application/json`
  * Protocol Buffers (optional): `application/x-protobuf`
  * JSON is the default. 
* The server responds with one of the following HTTP response codes:
  * `200` - Successful
  * `401` - Authentication required
  * `4XX` - A different client error
  * `5XX` - Server error
* If the RPC is successful, the server will encode the result and return it in the HTTP response body using the same encoding as the request.

## Why Protocol Buffers?

Some people like to use JSON alone, but JSON is a wire format, not an
API definition language. [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/proto3) 
are two things:

1. A clean, simple, mature way to define APIs
2. A binary wire format

You can generate client  and server code and API documentation from 
Protocol Buffer definitions, saving effort, and ensuring everything stays in 
sync. You can use JSON as the wire format, or you can use the more efficient Protocol Buffer binary 
format, which also enables you to change field names without breaking existing 
clients.

## What about gRPC?

gRPC depends on HTTP/2 which doesn't work everywhere (App Engine, Cloud Functions, 
etc.). [gRPC-Web](https://github.com/grpc/grpc-web) is complicated, non-idiotmatic,
and doesn't help here. Present RPC supports HTTP 1.x, it can benefit from
the performance improvements in HTTP/2 (just like gRPC), and its libraries can support
other transports entirely. Present RPC doesn't support streaming yet,
but it can in the future (please send a patch!). Present RPC is trivial to
understand, support, and use.