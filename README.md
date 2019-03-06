# Present RPC

Simple, idiomatic, [Protocol Buffer-based](https://developers.google.com/protocol-buffers/docs/proto3) RPCs

## Features

* Simple! Post a JSON object, and receive a JSON object back.
* Clearly define APIs using Protocol Buffers.
* REST-friendly: Use your familiar tools.
* Optionally use Protocol Buffer encoding (instead of JSON). It's more efficient, and you can safely rename fields.
* Transport agnostic (currently supports 
HTTP, more to come!)
* [Java client and server libraries](https://github.com/presentco/present-rpc/tree/master/java)
* Works with [App Engine](https://cloud.google.com/appengine/docs/java/)!

## By Example

Here's a Protocol Buffer definition for a service that echoes a value back to you:

```
service EchoService {
  rpc echo(EchoMessage) returns (EchoMessage);
}

message EchoMessage {
  uint32 value = 1;
}
```

You can call it with `curl` like so:

```
$ curl -i -H "Content-Type: application/json" -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```

That's it!

## HTTP Protocol Specification

* The client encodes the RPC argument and `POST`s it in the HTTP request body to `/[Service Name]/[Method Name]`.
* The client may specify an encoding in the `Content-Type` request header: 
  * JSON: `application/json`
  * Protocol Buffers: `application/x-protobuf`
  * JSON is the default. 
* The server responds with one of the following HTTP response codes:
  * `200` - Successful
  * `4XX` - Client Error
  * `5XX` - Server Error
* If the RPC is successful, the server will encode the result and return it in the HTTP response body using the same encoding as the request.
