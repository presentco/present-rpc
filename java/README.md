# Present RPC for Java

Present RPC for Java builds upon [Wire](https://github.com/square/wire),
a clean, lightweight Protocol Buffer compiler for Java.

## Modules

* `compiler`: Generate Java and Javascript code for Protocol Buffer services
* `client`: Call Present RPC services from Java
* `server`: Implement Present RPC services in Java

## Service Generation

The [`present-rpc-compiler`](https://github.com/presentco/present-rpc/blob/master/java/rpc-compiler/README.md) Gradle plugin generates code from protos in
`src/main/proto` and automatically compiles it along with your other
Java code. To use the plugin, add this code to your `build.gradle` file:

```groovy
buildscript {
  dependencies {
    classpath 'co.present.rpc:compiler:0.1-SNAPSHOT'
  }
}

apply plugin: 'present-rpc-compiler'
```

Don't use Gradle? Try the [command line tool](https://github.com/presentco/present-rpc/blob/master/java/rpc-compiler/README.md).

## Example: `EchoService`

Put `echo.proto` in `src/main/proto`:

```proto
service EchoService {
  rpc echo(EchoMessage) returns (EchoMessage);
}

message EchoMessage {
  uint32 value = 1;
}
```

The `present-rpc-compiler` plugin will generate `EchoService.java`:

```java
public interface EchoService {
  EchoMessage echo(EchoMessage request) throws IOException;
}
```

### The Server

Add `server` dependency to `build.gradle`:

```groovy
dependencies {
  implementation 'co.present.rpc:server:0.1-SNAPSHOT'
}
```

Implement `EchoService`:

```java
public class EchoServiceImpl implements EchoService {
  @Override public EchoMessage echo(EchoMessage request) {
    return new EchoMessage(request.value);
  }
}
```

Extend `RpcFilter`. This exposes your service and chosen implementation.

```java
public class EchoFilter extends RpcFilter {{
  service(EchoService.class, new EchoServiceImpl(), null);
}}
```

**Note:** The double curly braces (`{{ ... }}`) are shorthand for a constructor.

To add another service, simply call `service()` again.

Finally, map it in `web.xml`:

```xml
<filter>
  <filter-name>rpcFilter</filter-name>
  <filter-class>EchoFilter</filter-class>
</filter>

<filter-mapping>
  <filter-name>rpcFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

`RpcFilter` handles calls to your service and lets other unrelated requests pass through.

## The Client

Add `client` dependency to `build.gradle`:

```groovy
dependencies {
  implementation 'co.present.rpc:client:0.1-SNAPSHOT'
}
```

Create a client stub and call it:

```java
EchoService echo = RpcClient.create("http://localhost:8080", EchoService.class, null);
EchoResponse response = echo.echo(new EchoRequest(42));
assertEquals(42, (int) response.value);
```

## Headers

To get and set request headers, implement an `RpcInterceptor`.