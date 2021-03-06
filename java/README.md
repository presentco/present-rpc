# Present RPC for Java

Present RPC for Java builds upon [Wire](https://github.com/square/wire),
a clean, lightweight Protocol Buffer compiler for Java.

Table of Contents
=================

  * [Modules](#modules)
  * [Service Generation](#service-generation)
  * [Example: EchoService](#example-echoservice)
  * [The Server](#the-server)
  * [The Client](#the-client)
  * [Headers](#headers)

## Modules

* `compiler`: Generate Java and Javascript code for Protocol Buffer services
* `client`: Call Present RPC services from Java
* `server`: Implement Present RPC services in Java

## Service Generation

The [`present-rpc-compiler`](https://github.com/presentco/present-rpc/blob/master/java/compiler/README.md) Gradle plugin generates code from protos in
`src/main/proto` and automatically compiles it along with your other
Java code. To use the plugin, add this code to your `build.gradle` file:

```groovy
buildscript {
  dependencies {
    classpath 'co.present.rpc:compiler:0.1'
  }
}

apply plugin: 'co.present.rpc.compiler'
```

### Homebrew

Don't use Gradle? Install the [command line tool](https://github.com/presentco/present-rpc/blob/master/java/compiler/README.md) using brew:

`brew install presentco/utils/present-rpc`

This installs the `present-rpc` compiler command.  Run it with no arguments to see a help message.

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
  implementation 'co.present.rpc:server:0.1'
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

### Cross-Site Requests

If your web page is hosted on separate domain from your service, you can allow requests from 
other hosts:

```java
public class EchoFilter extends RpcFilter {{
  ...
  allowHost("localhost");
}}
```

Or even requests from any host:

```java
public class EchoFilter extends RpcFilter {{
  ...
  allowAll();
}}
```

## The Client

Add `client` dependency to `build.gradle`:

```groovy
dependencies {
  implementation 'co.present.rpc:client:0.1'
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
