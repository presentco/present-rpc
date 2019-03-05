# Present RPC for Java

Present RPC for Java builds upon [Wire](https://github.com/square/wire).

## Modules

* `rpc-compiler`: Generates Java interfaces from Protocol Buffer services  
* `rpc-client`: Java client to call Present RPC services
* `rpc-server`: Implement Present RPC services in Java

## Service Generation Example

Put `echo.proto` in `src/main/proto`:

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

Call `rpc-compiler` from `build.gradle`. It will generate service interfaces and delegate to Wire
to generate classes for Protocol Buffer messages:

```
ext {
  generatedSourcesDir = "${buildDir}/generated/src/java"
}

configurations {
  rpc
}

dependencies {
  rpc 'co.present.present-rpc:rpc-compiler:0.1-SNAPSHOT'
}

task generateProtos(type: JavaExec) {
  classpath = configurations.rpc
  main = 'present.rpc.RpcCompiler'
  args = [
          '--proto_path=src/main/proto',
          "--java_out=$generatedSourcesDir"
  ]
}

compileJava {
  dependsOn generateProtos
}

sourceSets.main.java.srcDirs += generatedSourcesDir

```

This generates `EchoService.java`:

```
public interface EchoService {
  EchoResponse echo(EchoRequest request) throws IOException;
}
```

## Server Example

Add `rpc-server` to `build.gradle`:

```
dependencies {
  implementation 'co.present.present-rpc:rpc-server:0.1-SNAPSHOT'
}
```

Implement `EchoService`:

```
public class EchoServiceImpl implements EchoService {
  @Override public EchoResponse echo(EchoRequest request) {
    return new EchoResponse(request.value);
  }
}
```

Extend `RpcFilter`. This exposes your service with your chosen implementation.

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

`RpcFilter` handles calls to your service and lets other requests pass through.

## Client Example

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
