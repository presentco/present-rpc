package present.rpc;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Charsets;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Mustache utilities
 *
 * @author Bob Lee
 */
class Mustaches {

  private Mustaches() {}

  private static final MustacheFactory factory = new DefaultMustacheFactory();

  public static Mustache compileResource(String path) {
    return factory.compile(
        new InputStreamReader(Mustaches.class.getResourceAsStream(path), Charsets.UTF_8), path);
  }

  public static String toString(Mustache template, Object scope) {
    StringWriter out = new StringWriter();
    template.execute(out, scope);
    return out.toString();
  }
}
