# Present RPC Javascript Generator

Generates Javascript code for [Present RPC](https://github.com/presentco/present-rpc) services

## Example

If you run this tool against `echo.proto`:

```proto
service EchoService {
  rpc echo(EchoMessage) returns (EchoMessage);
}

message EchoMessage {
  uint32 value = 1;
}
```

It will generate `echo.js` which you can use to invoke a remote `EchoService`:

```javascript
var service = new EchoService('http://localhost:8080');
service.echo({value: 42}).then(function(result) {
  alert('The answer is ' + result.value + '.');
});
```

As you can see, the generated methods return `Promise`s.

You can set request headers like so:

```javascript
service.headers = {
  'Name', 'Value'
};
```

[Here's a complete example](https://github.com/presentco/present-rpc/tree/master/java/example).

## Download

```

```
