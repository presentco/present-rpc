package present.rpc;

import com.squareup.wire.Message;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A method in a service interface.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcMethod {

  private final Method method;
  private final Class<?> argumentType;
  private final Class<?> resultType;

  public RpcMethod(Method method) {
    this.method = method;
    Preconditions.checkArgument(method.getParameterTypes().length == 1);
    this.argumentType = method.getParameterTypes()[0];
    this.resultType = method.getReturnType();
    Preconditions.checkArgument(Message.class.isAssignableFrom(this.argumentType));
    Preconditions.checkArgument(Message.class.isAssignableFrom(this.resultType));
  }

  public Class<?> service() { return method.getDeclaringClass(); }

  public Method method() {
    return method;
  }

  public Class<?> argumentType() {
    return argumentType;
  }

  public Class<?> resultType() {
    return resultType;
  }

  /** Maps method names to {@code ServiceMethod}s for a given service. */
  public static Map<String, RpcMethod> mapFor(Class<?> service) {
    Preconditions.checkArgument(service.isInterface());
    HashMap<String, RpcMethod> map = new HashMap<>();
    for (Method method : service.getMethods()) {
      map.put(method.getName(), new RpcMethod(method));
    }
    return Collections.unmodifiableMap(map);
  }

  @Override public String toString() {
    return method.getDeclaringClass().getSimpleName() + "." + method.getName();
  }
}
