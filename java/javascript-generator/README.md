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

As you can see, the generated methods return [`Promise`s](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise).

You can set request headers like so:

```javascript
service.headers = {
  'Name', 'Value'
};
```

[Here's a complete example](https://github.com/presentco/present-rpc/tree/master/java/example).

## Usage

Download [present-rpc-javascript-generator.jar](https://github.com/presentco/present-rpc/raw/master/java/javascript-generator/build/libs/present-rpc-javascript-generator.jar).

```bash
Usage: java -jar present-rpc-javascript-generator.jar [--out=<out>]
                                                      [--protos=<protos>]...
                                                      [<files>...]
      --protos=<protos>   protos directory
      --out=<out>         output directory
      [<files>...]        proto files (optional)
```

## Gradle

The `present-rpc-javascript-generator` Gradle plugin will generate Javascript
in `build/js` from protos in `src/main/proto`:

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'co.present.present-rpc:javascript-generator:0.1-SNAPSHOT'
  }
}

repositories {
  jcenter()
}

// Generates 'build/js/echo.js'. Adds 'generateJavascriptServices' task.
apply plugin: 'present-rpc-javascript-generator'
apply plugin: 'war'

// Generate Javascript services and include them in the war.
war {
  dependsOn 'generateJavascriptServices'
  from 'build/js'
}
```
