package present.rpc;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okio.ByteString;

/**
 * A method in a service interface.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcMethod {

  private final Method method;
  private final Class<?> argumentType;
  private final Class<?> resultType;
  private final ProtoAdapter argumentAdapter;
  private final ProtoAdapter resultAdapter;

  public RpcMethod(Method method) {
    this.method = method;
    Preconditions.checkArgument(method.getParameterTypes().length == 1);
    this.argumentType = method.getParameterTypes()[0];
    this.resultType = method.getReturnType();
    Preconditions.checkArgument(Message.class.isAssignableFrom(this.argumentType));
    Preconditions.checkArgument(Message.class.isAssignableFrom(this.resultType));
    argumentAdapter = ProtoAdapter.get(this.argumentType);
    resultAdapter = ProtoAdapter.get(resultType);
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

  public Message argumentFromBytes(ByteString argument) throws IOException {
    return (Message) argumentAdapter.decode(argument);
  }

  public ByteString argumentToBytes(Message argument) {
    return toBytes(argument, argumentAdapter, method.getParameterTypes()[0]);
  }

  public Message resultFromBytes(ByteString result) throws IOException {
    return (Message) resultAdapter.decode(result);
  }

  public ByteString resultToBytes(Object result) {
    return toBytes(result, resultAdapter, method.getReturnType());
  }

  private static ByteString toBytes(Object value, ProtoAdapter adapter, Class<?> expectedType) {
    expectedType.cast(value);
    // The case above ensures this is OK.
    @SuppressWarnings("unchecked")
    byte[] bytes = adapter.encode(value);
    return ByteString.of(bytes);
  }

  @Override public String toString() {
    return method.getDeclaringClass().getSimpleName() + "." + method.getName();
  }
}
