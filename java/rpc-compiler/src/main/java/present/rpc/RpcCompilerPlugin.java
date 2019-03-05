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
import org.gradle.api.tasks.SourceSet;

/**
 * Gradle plugin for the RPC compiler.
 *
 * @author Bob Lee
 */
public class RpcCompilerPlugin implements Plugin<Project> {

  private static final String GENERATE_PROTOS = "generateProtos";

  @Override public void apply(Project project) {
    File projectDir = project.getProjectDir();
    File protoSrc = new File(projectDir, "src/main/proto");

    File buildDir = project.getBuildDir();
    File generatedSrc = new File(buildDir, "proto");

    project.getTasks().register(GENERATE_PROTOS, new Action<Task>() {
      @Override public void execute(Task task) {
        try {
          RpcCompiler.main(new String[] {
            "--java_out=" + generatedSrc.getPath(),
            "--proto_path=" + protoSrc.getPath(),
          });
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage());
        }
      }
    });

    project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
      JavaPluginConvention javaConvention =
          project.getConvention().getPlugin(JavaPluginConvention.class);
      SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
      main.getJava().srcDir(generatedSrc);
      Set<Task> compileTasks = project.getTasksByName(JavaPlugin.COMPILE_JAVA_TASK_NAME,
          false);
      for (Task compileTask : compileTasks) {
        compileTask.dependsOn(GENERATE_PROTOS);
      }
    });
  }
}
