# Present RPC Compiler

Generates Java and Javascript code for [Present RPC](https://github.com/presentco/present-rpc) 
services

## Command Line Usage

Download [present-rpc-compiler.jar](https://github.com/presentco/present-rpc/raw/master/java/javascript-generator/build/libs/present-rpc-javascript-generator.jar). 

Run `java -jar present-rpc-compiler.jar`. It supports the following parameters:

* `--proto_path=<dir>` Directory containing Protocol Buffers (required)
* `--java_out=<dir>` Java output directory (optional)
* `--js_out=<dir>` Javascript output directory (optional)

The compiler delegates to Wire's compiler to generate Protocol Buffer messages
in Java, so its command line options are supported, too.

## Gradle

The Present RPC Gradle plugin reads protos from `src/main/proto`
and outputs Java to `build/java`.

Run the `presentRpcJavascript` task to output Javascript to `build/js`, too. 

For example:

```groovy
buildscript {
  dependencies {
    classpath 'co.present.present-rpc:javascript-generator:0.1-SNAPSHOT'
  }
}

// Output Java interfaces to build/java.
apply plugin: 'co.present.rpc.compiler'

apply plugin: 'war'

war {
  // Output Javascript to build/js before we build a war.
  dependsOn 'presentRpcJavascript'

  // Include the generated Javascript in the war.
  from 'build/js'
}
```
