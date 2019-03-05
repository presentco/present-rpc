# Present RPC

Simple, idiomatic, Protocol Buffer-based RPCs

## Features

* Simple! Post a JSON object, and receive a JSON object back.
* Clearly define APIs using Protocol Buffers.
* REST-friendly: Use your familiar tools.
* Use Protocol Buffer encoding for more efficiency and the ability to rename fields.
* Transport agnostic (currently supports 
HTTP)
* [Java client and server implementations](https://github.com/presentco/present-rpc/tree/master/java)
* Works with [App Engine](https://cloud.google.com/appengine/docs/java/)

## HTTP Protocol

* Specify the encoding in the `Content-Type` request header: 
  * JSON: `application/json`
  * Protocol Buffers: `application/x-protobuf`
* POST the encoded argument to `/[Service Name]/[Method Name]`. 
* The service will respond with one of the following HTTP response codes:
  * `200` - Successful
  * `4XX` - Client Error
  * `5XX` - Server Error
* If successful (`200`), the service will return the 
encoded result in the response body using the same encoding as the request.

For a Protocol Buffer service:

```
service [Service Name] {
  rpc [Method Name]([Argument Type]) returns ([Result Type]);
}
```

An HTTP request looks like:

```
POST /[Service Name]/[Method Name]
Content-Type: [Content Type]

[Encoded Argument]
```

The the response looks like:

```
HTTP/1.1 200 OK
Content-Type: [Content Type]

[Encoded Result]
```

### Example

Call this service:

```
service EchoService {
  rpc echo(EchoRequest) returns (EchoResponse);
}

message EchoRequest {
  uint32 value = 1;
}

message EchoResponse {
  uint32 value = 1;
}
```

With `curl`:

```
$ curl -i -H "Content-Type: application/json" -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```