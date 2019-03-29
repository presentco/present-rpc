package present.rpc;

import com.github.mustachejava.Mustache;
import com.google.common.collect.Lists;
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
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import picocli.CommandLine;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class JavascriptGenerator {

  private static final Mustache template = Mustaches.compileResource("template.mjs");

  private final Log log;
  private final Schema schema;
  private final File outputDirectory;

  public JavascriptGenerator(Log log, Schema schema, String outputDirectory) {
    this.log = log;
    this.schema = schema;
    this.outputDirectory = new File(outputDirectory);
  }

  void execute() throws IOException {
    this.outputDirectory.mkdirs();
    String library = "present-rpc.mjs";
    Files.copy(JavascriptGenerator.class.getResourceAsStream(library),
        new File(this.outputDirectory, library).toPath(), StandardCopyOption.REPLACE_EXISTING);
    for (ProtoFile protoFile : schema.protoFiles()) {
      if (protoFile.services().isEmpty()) continue;
      File jsFile = new File(outputDirectory, protoFile.name() + ".mjs");
      try (Writer out = new OutputStreamWriter(new BufferedOutputStream(
          new FileOutputStream(jsFile)), StandardCharsets.UTF_8)) {
        // We wouldn't need this view if mustache.java supported "../foo" syntax.
        template.execute(out, new ProtoFileView(protoFile));
      }
      log.info("Generated %s.", jsFile);
    }
  }

  public static class ProtoFileView {
    private final ProtoFile file;

    private ProtoFileView(ProtoFile file) {
      this.file = file;
    }

    public List<ServiceView> services() {
      return Lists.transform(file.services(), ServiceView::new);
    }

    public String fileName() {
      return file.location().path();
    }
  }

  public static class ServiceView {
    private final Service service;

    public ServiceView(Service service) {
      this.service = service;
    }

    public String serviceName() {
      return service.name();
    }

    public List<RpcView> rpcs() {
      return Lists.transform(service.rpcs(), RpcView::new);
    }
  }

  public static class RpcView {
    private final Rpc rpc;

    public RpcView(Rpc rpc) {
      this.rpc = rpc;
    }

    public String rpcName() {
      return rpc.name();
    }
  }
}
