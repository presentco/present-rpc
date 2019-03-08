# Server Example

Demonstrates a Javascript client running against a Java server.

## Run the Server

```
$ gradle appRun
```

## Call the Server

Run:

```
$ curl -i -H "Content-Type: application/json" -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```

Or open [http://localhost:8080/](http://localhost:8080/).