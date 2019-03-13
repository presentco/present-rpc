
package present.rpc;

import com.github.mustachejava.Mustache;
import com.google.common.collect.Sets;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Rpc;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.SchemaLoader;
import com.squareup.wire.schema.Service;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import picocli.CommandLine;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

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
      File jsFile = new File(outputDirectory, protoFile.name() + ".js");
      try (Writer out = new OutputStreamWriter(new BufferedOutputStream(
          new FileOutputStream(jsFile)), StandardCharsets.UTF_8)) {
        for (Service service : protoFile.services()) {
          Map<String, String> scope = new HashMap<>();
          scope.put("SourceFile", protoFile.location().path());

          scope.put("ServiceName",
              protoFile.packageName() != null ? protoFile.packageName() + "_" + service.name() :
                  service.name());

          out.write(Mustaches.toString(serviceTemplate, scope));
          out.write('\n');
          for (Rpc rpc : service.rpcs()) {
            scope.put("MethodName", upperToLowerCamel(rpc.name()));
            out.write(Mustaches.toString(methodTemplate, scope));
            out.write('\n');
          }
        }
      }
      log.info("Generated %s.", jsFile);
    }
  }

  private String upperToLowerCamel(String string) {
    return UPPER_CAMEL.to(LOWER_CAMEL, string);
  }
}

