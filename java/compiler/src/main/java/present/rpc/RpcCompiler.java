package present.rpc;

import com.squareup.wire.WireCompiler;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.SchemaLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import picocli.CommandLine;

import static picocli.CommandLine.Option;

/**
 * Generates Java and Javascript code from Protocol Buffers.
 *
 * @author Bob Lee
 */
@CommandLine.Command(
    name = "java -jar present-rpc-compiler.jar",
    mixinStandardHelpOptions = true,
    version = "0.1",
    header = "Generate Java and/or Javascript from Protocol Buffers"
)
public class RpcCompiler {

  private static final Logger logger = Logger.getLogger(RpcCompiler.class.getName());

  @Option(names = "--js_out", description = "Javascript output directory")
  private String jsOut;

  @Option(names = "--java_out", description = "Java output directory")
  private String javaOut;

  @Option(names = "--proto_path", required = true)
  private List<String> protoPaths = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    // Generate services
    RpcCompiler compiler = new RpcCompiler();

    if (args.length == 0) {
      CommandLine.usage(compiler, System.out);
      System.exit(0);
    }

    try {
      CommandLine cl = new CommandLine(compiler);
      cl.setUnmatchedArgumentsAllowed(true);
      cl.parse(args);
    }
    catch (CommandLine.ParameterException e) {
      System.err.println(e.getMessage());
      CommandLine.usage(compiler, System.out);
      System.exit(0);
    }

    // Parse protos
    SchemaLoader loader = new SchemaLoader();
    for (String source : compiler.protoPaths) loader.addSource(new File(source));
    Schema schema = loader.load();

    if (compiler.javaOut != null) {
      // Generate messages
      WireCompiler.main(args);

      // Generate services
      new JavaGenerator(System.out::printf, schema, compiler.javaOut).execute();
    }

    if (compiler.jsOut != null) {
      new JavascriptGenerator(System.out::printf, schema, compiler.jsOut).execute();
    }
  }
}
