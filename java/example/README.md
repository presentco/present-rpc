# Server Example

## Run the Server

```
$ gradle run
```

## Call the Server

```
curl -i -H "Content-Type: application/json" -X POST -d '{value:42}' http://localhost:8080/EchoService/echo
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 12

{"value":42}
```