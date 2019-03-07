package present.rpc;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.War;

/**
 * Gradle plugin for the Javascript generator.
 *
 * @author Bob Lee
 */
public class JavascriptGeneratorPlugin implements Plugin<Project> {

  private static final String GENERATE_JAVASCRIPT = "generateJavascriptServices";

  @Override public void apply(Project project) {
    File projectDir = project.getProjectDir();
    File protoSrc = new File(projectDir, "src/main/proto");

    File buildDir = project.getBuildDir();
    File generatedSrc = new File(buildDir, "js");

    project.getTasks().register(GENERATE_JAVASCRIPT, new Action<Task>() {
      @Override public void execute(Task task) {
        try {
          JavascriptGenerator.main(new String[] {
            "--out=" + generatedSrc.getPath(),
            "--protos=" + protoSrc.getPath(),
          });
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage());
        }
      }
    });
  }
}
