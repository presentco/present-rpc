package present.rpc;

import com.google.common.base.Splitter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Exposes a protocol buffer service over HTTP. Path: "/ServiceName/methodName"
 *
 * Defaults to protocol buffer encoding. Add a "Content-Type: application/json" header to the
 * request to use JSON encoding instead.
 *
 * JSON request and response wrappers have the same shape as the wrappers in rpc.proto except
 * they use JSON objects instead of bytes.
 *
 * @author Bob Lee (bob@present.co)
 */
public abstract class RpcServlet extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(RpcServlet.class);

  private final RpcHandler jsonHandler;
  private final RpcHandler protoHandler;

  public RpcServlet() {
    this.jsonHandler = new JsonHandler();
    this.protoHandler = new ProtoHandler();
  }

  private final Map<String, RpcService> services = new HashMap<>();

  /** Declares a service. Called from subclass constructors. */
  protected <T> void service(Class<?> headerType, Class<T> interfaceType, T implementation,
      RpcFilter filter) {
    service(headerType, interfaceType, implementation, filter, Collections.emptyMap());
  }

  /** Declares a service. Called from subclass constructors. */
  protected <T> void service(Class<?> headerType, Class<T> interfaceType, T implementation,
      RpcFilter filter, Map<String, String> aliases) {
    checkNotNull(interfaceType);
    checkNotNull(implementation);

    String name = interfaceType.getSimpleName();
    checkState(!services.containsKey(name), "'%s' is already mapped.", name);
    services.put(name, new RpcService(headerType, interfaceType, implementation, filter, aliases));
  }

  @Override protected void doPost(HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) throws IOException {
    String pathInfo = httpRequest.getPathInfo();
    List<String> pathElements = Splitter.on('/').splitToList(pathInfo);
    if (pathElements.size() != 3 || !pathElements.get(0).equals("")) {
      logger.error("Bad path: {}", pathInfo);
      httpResponse.sendError(400, "Bad path");
      return;
    }
    String serviceName = pathElements.get(1);
    RpcService service = services.get(serviceName);
    if (service == null) {
      logger.error("Unknown service: {}", serviceName);
      httpResponse.sendError(400, "Unknown service");
      return;
    }
    String rpcName = pathElements.get(2);
    rpcName = firstNonNull(service.aliases.get(rpcName), rpcName);
    final RpcMethod method = service.methods.get(rpcName);
    if (method == null) {
      logger.error("Unknown RPC: {}.{}", serviceName, rpcName);
      httpResponse.sendError(400, "Unknown RPC");
      return;
    }
    // Default to protocol buffers.
    RpcHandler handler;
    String contentType = httpRequest.getContentType();
    // Example: "application/json; charset=utf-8"
    if (contentType != null && contentType.contains(RpcProtocol.JSON.contentType)) {
      handler = jsonHandler;
    } else {
      handler = protoHandler;
    }
    handler.handle(httpRequest, httpResponse, service, method);
  }
}
