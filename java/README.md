# Present RPC for Java

Present RPC for Java builds upon [Wire](https://github.com/square/wire),
a clean, lightweight Protocol Buffer compiler for Java.

## Modules

* `rpc-compiler`: Generate Java code for Protocol Buffer services
* `rpc-client`: Call Present RPC services from Java
* `rpc-server`: Implement Present RPC services in Java

## Service Generation

The `present-rpc-compiler` Gradle plugin generates code from protos in
`src/main/proto` and automatically compiles it along with your other
Java code. To use the plugin, add this code to your `build.gradle` file:

```
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'co.present.present-rpc:rpc-compiler:0.1-SNAPSHOT'
  }
}

apply plugin: 'present-rpc-compiler'
```

Don't use Gradle? Try the `present.rpc.RpcCompiler` command line tool instead.

## Example: `EchoService`

Put `echo.proto` in `src/main/proto`:

```
service EchoService {
  rpc echo(EchoMessage) returns (EchoMessage);
}

message EchoMessage {
  uint32 value = 1;
}
```

The `present-rpc-compiler` plugin will generate `EchoService.java`:

```
public interface EchoService {
  EchoMessage echo(EchoMessage request) throws IOException;
}
```

### The Server

Add `rpc-server` to `build.gradle`:

```
dependencies {
  implementation 'co.present.present-rpc:rpc-server:0.1-SNAPSHOT'
}
```

Implement `EchoService`:

```
public class EchoServiceImpl implements EchoService {
  @Override public EchoMessage echo(EchoMessage request) {
    return new EchoMessage(request.value);
  }
}
```

Extend `RpcFilter`. This exposes your service and chosen implementation.

```
public class EchoFilter extends RpcFilter {{
  service(EchoService.class, new EchoServiceImpl(), null);
}}
```

Finally, map it in `web.xml`:

```
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

Add `rpc-client` to `build.gradle`:

```
dependencies {
  implementation 'co.present.present-rpc:rpc-client:0.1-SNAPSHOT'
}
```

Create a client stub and call it:

```
EchoService echo = RpcClient.create("http://localhost:8080", EchoService.class, null);
EchoResponse response = echo.echo(new EchoRequest(42));
assertEquals(42, (int) response.value);
```

That's it!