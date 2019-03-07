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

final class JavascriptGenerator {

  private static final Mustache serviceTemplate = Mustaches.compileResource("service.js");
  private static final Mustache methodTemplate = Mustaches.compileResource("method.js");

  private final Log log;
  private final Set<String> sources;
  private final Set<String> protos;
  private final String generatedSourceDirectory;

  JavascriptGenerator(Log log, Set<String> sources,
      Set<String> protos, String generatedSourceDirectory) {
    this.log = log;
    this.sources = sources;
    this.protos = protos;
    this.generatedSourceDirectory = generatedSourceDirectory;
  }

  void execute() throws IOException {
    Schema schema = loadSchema();
    new File(generatedSourceDirectory).mkdirs();
    for (ProtoFile protoFile : schema.protoFiles()) {
      File jsFile = new File(generatedSourceDirectory, protoFile.name() + ".js");
      try (Writer out = new OutputStreamWriter(new BufferedOutputStream(
          new FileOutputStream(jsFile)), StandardCharsets.UTF_8)) {
        for (Service service : protoFile.services()) {
          Map<String, String> scope = new HashMap<>();
          scope.put("SourceFile", protoFile.location().path());
          scope.put("ServiceName", service.name());
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

  private Schema loadSchema() throws IOException {
    SchemaLoader loader = new SchemaLoader();
    for (String source : sources) loader.addSource(new File(source));
    for (String proto : protos) loader.addProto(proto);
    return loader.load();
  }

  private String upperToLowerCamel(String string) {
    return UPPER_CAMEL.to(LOWER_CAMEL, string);
  }

  interface Log {
    void info(String format, Object... args);
  }

  private static class Args {
    @CommandLine.Parameters
    private List<String> files = new ArrayList<>();

    @CommandLine.Option(names = "--out")
    private String out;

    @CommandLine.Option(names = "--protos")
    private List<String> protos = new ArrayList<>();
  }

  public static void main(String[] args) throws IOException {
    Args parsed = new Args();
    if (args.length == 0) {
      CommandLine.usage(parsed, System.out);
      System.exit(0);
    }

    // Generate services
    CommandLine.populateCommand(parsed, args);
    new JavascriptGenerator(System.out::printf, Sets.newHashSet(parsed.protos),
        Sets.newHashSet(parsed.files), parsed.out).execute();
  }
}
