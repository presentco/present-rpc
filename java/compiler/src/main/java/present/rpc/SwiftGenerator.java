package present.rpc;

import com.github.mustachejava.Mustache;
import com.google.common.base.Ascii;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.ProtoType;
import com.squareup.wire.schema.Rpc;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.Service;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

@SuppressWarnings("ALL")
public class SwiftGenerator {

  private static final Mustache serviceTemplate = Mustaches.compileResource("service.swift");
  private static final Mustache methodTemplate = Mustaches.compileResource("method.swift");

  private final Log log;
  private final Schema schema;
  private final String outputDirectory;

  public SwiftGenerator(Log log, Schema schema, String outputDirectory) {
    this.log = log;
    this.schema = schema;
    this.outputDirectory = outputDirectory;
  }

  void execute() throws IOException {
    new File(outputDirectory).mkdirs();
    for (ProtoFile protoFile : schema.protoFiles()) {
      if (protoFile.services().isEmpty()) continue;
      File swiftFile = new File(outputDirectory, protoFile.name() + ".swift");
      try (Writer out = new OutputStreamWriter(new BufferedOutputStream(
          new FileOutputStream(swiftFile)), StandardCharsets.UTF_8))
      {
        for (Service service : protoFile.services()) {
          Map<String, String> scope = new HashMap<>();
          scope.put("SourceFile", protoFile.location().path());
          scope.put("ServiceName",
              protoFile.packageName() != null ? packageNameToSwift(protoFile.packageName()) + "_" + service.name() :
                  service.name());

          out.write(Mustaches.toString(serviceTemplate, scope));
          out.write('\n');

          for (Rpc rpc : service.rpcs()) {
            scope.put("MethodName", upperToLowerCamel(rpc.name()));
            scope.put("RequestType", rpcTypeNameToSwift(rpc.requestType()));
            scope.put("ResponseType", rpcTypeNameToSwift(rpc.responseType()));
            scope.put("Documentation", rpc.documentation());
            out.write(Mustaches.toString(methodTemplate, scope));
            out.write('\n');
          }
        }
      }
      log.info("Generated %s.\n", swiftFile);
    }
  }

  // e.g. example.EchoService -> Example_EchoService
  // Note: Attempting to match the Apple Swift Protobuf compiler conventions
  private static String rpcTypeNameToSwift(ProtoType type) {
     return lowerCamelToUpperCamel(type.enclosingTypeOrPackage()) + "_" + type.simpleName();
  }

  // e.g. foo.bar -> FooBar
  // Note: Attempting to match the Apple Swift Protobuf compiler conventions
  private static String packageNameToSwift(String string) {
    String [] words = string.split("\\.");
    return Arrays.stream(words).map(w -> firstCharOnlyToUpper(w)).collect(Collectors.joining());
  }

  private static String upperToLowerCamel(String string) {
    return UPPER_CAMEL.to(LOWER_CAMEL, string);
  }

  private static String lowerCamelToUpperCamel(String string) {
    return LOWER_CAMEL.to(UPPER_CAMEL, string);
  }

  private static String firstCharOnlyToUpper(String word) {
    return (word.isEmpty())
        ? word
        : new StringBuilder(word.length())
            .append(Ascii.toUpperCase(word.charAt(0)))
            .append(Ascii.toLowerCase(word.substring(1)))
            .toString();
  }

}

