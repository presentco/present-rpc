package present.rpc;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate-sources", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class WireServiceGeneratorMojo extends AbstractMojo implements WireServiceGenerator.Log {
  @Parameter(property = "wireServiceGenerator.protoPaths", required = true)
  private String[] protoPaths;

  @Parameter(property = "wireServiceGenerator.protoFiles", required = true)
  private String[] protoFiles;

  @Parameter(
      property = "wireServiceGenerator.generatedSourceDirectory",
      defaultValue = "${project.build.directory}/generated-sources/wire-service")
  private String generatedSourceDirectory;

  @Parameter(
      defaultValue = "${project}",
      required = true,
      readonly = true)
  private MavenProject project;

  @Override public void execute() throws MojoExecutionException, MojoFailureException {
    project.addCompileSourceRoot(generatedSourceDirectory);

    ImmutableSet<String> protoPathsSet = ImmutableSet.copyOf(protoPaths);
    ImmutableSet<String> protoFilesSet = ImmutableSet.copyOf(protoFiles);

    try {
      WireServiceGenerator codeGenerator = new WireServiceGenerator(
          this, protoPathsSet, protoFilesSet, generatedSourceDirectory);
      codeGenerator.execute();
    } catch (IOException e) {
      throw new MojoExecutionException("failed to generate sources", e);
    }
  }

  @Override public void info(String format, Object... args) {
    getLog().info(String.format(format, args));
  }
}
