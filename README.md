# Present RPC

Simple Protocol Buffer-based RPCs

## Features

* Simple!
* Transport agnostic (currently supports 
HTTP)
* REST-friendly
* [Java client and server implementations](https://github.com/presentco/present-rpc/tree/master/java)

## HTTP Protocol

* POST the encoded argument to `/[Service Name]/[Method Name]`. 
* The service will respond with one of the following codes:
  * 200 - Successful
  * 4XX - Client Error
  * 5XX - Server Error
* If successful (200), the service will return the 
encoded result in the response body.
* Defaults to JSON encoding. Set the `Content-Type` to `application/x-protobuf` to use Protocol
Buffer encoding.

### Example

Call this example service:

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

Using `curl`:

```
$ curl -i -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```