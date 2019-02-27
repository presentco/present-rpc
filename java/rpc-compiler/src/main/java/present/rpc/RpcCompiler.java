package present.rpc;

import com.google.common.collect.Sets;
import com.squareup.wire.WireCompiler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import picocli.CommandLine;

import static picocli.CommandLine.*;

/**
 * Generates Java code.
 *
 * @author Bob Lee
 */
public class RpcCompiler {

  private static final Logger logger = Logger.getLogger(RpcCompiler.class.getName());

  @Parameters
  private List<String> files = new ArrayList<>();

  @Option(names = "--java_out")
  private String javaOut;

  @Option(names = "--proto_path")
  private List<String> protoPaths = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    // Generate messages
    WireCompiler.main(args);

    // Generate services
    RpcCompiler compiler = new RpcCompiler();
    CommandLine.populateCommand(compiler, args);
    compiler.execute();
  }

  private void execute() throws IOException {
    new WireServiceGenerator(System.out::printf, Sets.newHashSet(protoPaths),
        Sets.newHashSet(files), javaOut).execute();
  }
}
