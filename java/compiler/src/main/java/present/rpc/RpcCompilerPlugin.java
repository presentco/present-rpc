package present.rpc;

import java.io.File;
import java.io.IOException;
import java.util.Set;
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

  private static final String GENERATE_JAVA = "presentRpcJava";
  private static final String GENERATE_JAVASCRIPT = "presentRpcJavascript";

  @Override public void apply(Project project) {
    File projectDir = project.getProjectDir();
    File protoSrc = new File(projectDir, "src/main/proto");

    File buildDir = project.getBuildDir();
    File javaOut = new File(buildDir, "java");
    File jsOut = new File(buildDir, "js");

    project.getTasks().register(GENERATE_JAVA, task -> {
      try {
        RpcCompiler.main(new String[] {
          "--java_out=" + javaOut.getPath(),
          "--proto_path=" + protoSrc.getPath(),
        });
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    });

    // Automaticaly compile generated Java.
    project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
      JavaPluginConvention javaConvention =
          project.getConvention().getPlugin(JavaPluginConvention.class);
      SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
      main.getJava().srcDir(javaOut);
      Set<Task> compileTasks = project.getTasksByName(JavaPlugin.COMPILE_JAVA_TASK_NAME, false);
      for (Task compileTask : compileTasks) compileTask.dependsOn(GENERATE_JAVA);
    });

    project.getTasks().register(GENERATE_JAVASCRIPT, task -> {
      try {
        RpcCompiler.main(new String[] {
            "--js_out=" + jsOut.getPath(),
            "--proto_path=" + protoSrc.getPath(),
        });
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
    });
  }
}
