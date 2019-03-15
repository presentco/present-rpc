package present.rpc;

import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
 * <p>Defaults to JSON encoding. Add a "Content-Type: application/x-protobuf" header to the
 * request to use protocol buffer encoding instead.
 *
 * @author Bob Lee (bob@present.co)
 */
public abstract class RpcFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(RpcFilter.class);

  private final Map<String, RpcService> services = new HashMap<>();

  private boolean allowAll = false;
  private Set<String> allowedHosts = new HashSet<>();

  /** Allows requests from the given host name. */
  protected void allowHost(String host) {
    allowedHosts.add(host);
  }

  /** Allows requests from all hosts. */
  protected void allowAll() {
    allowAll = true;
  }

  /** Declares a service. Called from subclass constructors. */
  protected <T> void service(Class<T> interfaceType, T implementation) {
    service(interfaceType, implementation, null);
  }

  /** Declares a service. Called from subclass constructors. */
  protected <T> void service(Class<T> interfaceType, T implementation, RpcInterceptor interceptor) {
    service(interfaceType, implementation, interceptor, Collections.emptyMap());
  }

  /** Declares a service. Called from subclass constructors. */
  protected <T> void service(
      Class<T> interfaceType,
      T implementation,
      RpcInterceptor interceptor,
      Map<String, String> aliases) {
    checkNotNull(interfaceType);
    checkNotNull(implementation);

    String name = interfaceType.getSimpleName();
    checkState(!services.containsKey(name), "'%s' is already mapped.", name);
    services.put(name, new RpcService(interfaceType, implementation, interceptor, aliases));
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    String path = request.getServletPath();
    List<String> pathElements = Splitter.on('/').splitToList(path);
    if (pathElements.size() == 3) {
      String serviceName = pathElements.get(1);
      RpcService service = services.get(serviceName);
      if (service != null) {
        // Handle cross-site requests.
        // Background: https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
        // Note: All requests will be pre-flighted.
        String origin = request.getHeader("Origin");
        if (origin != null) {
          URI originUri = URI.create(origin);
          if (allowAll || allowedHosts.contains(originUri.getHost())) {
            response.addHeader("Access-Control-Allow-Origin", origin);
            if ("OPTIONS".equals(request.getMethod().toUpperCase())) {
              response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
              response.addHeader("Access-Control-Max-Age", "86400"); // 1 day
              //
              response.addHeader("Access-Control-Allow-Credentials", "true");
              // Allow any request headers.
              String requestHeaders = request.getHeader("Access-Control-Request-Headers");
              if (requestHeaders != null) {
                response.addHeader("Access-Control-Allow-Headers", requestHeaders);
              }
            }
          } else {
            logger.info("Request from disallowed origin: " + origin);
          }
        }

        String methodName = pathElements.get(2);
        doRpc(request, response, service, methodName);
        return;
      }
    }
    chain.doFilter(servletRequest, servletResponse);
  }

  private void doRpc(HttpServletRequest request, HttpServletResponse response,
      RpcService service, String methodName) throws IOException {
    // Look up method by name.
    methodName = firstNonNull(service.aliases.get(methodName), methodName);
    final RpcMethod method = service.methods.get(methodName);
    if (method == null) {
      logger.info("Unknown RPC: {}.{}", service.name(), methodName);
      sendError(request, response, 404, "RPC method not found");
      return;
    }

    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      response.setStatus(200);
      return;
    }

    if (!"POST".equals(requestMethod)) {
      sendError(request, response, 400, "HTTP POST required");
      return;
    }

    // Determine encoding. Default to JSON.
    String contentType = request.getContentType();
    RpcEncoding encoding = RpcEncoding.forContentType(contentType);
    if (encoding == null) {
      sendError(request, response, 400, "Unsupported content type: " + contentType);
      return;
    }

    // Parse argument.
    Object argument;
    try {
      argument = encoding.decode(method.argumentType(), request.getInputStream());
    } catch (Exception e) {
      logger.info("Invalid request", e);
      sendError(request, response, 400, "Bad argument");
      return;
    }

    // Invoke RPC.
    Object result;
    try {
      result = service.invoke(new ServletRpcHeaders(request), method, argument);
    } catch (ClientException e) {
      logger.info("Client error", e);
      response.sendError(400, e.getMessage());
      return;
    } catch (Exception e) {
      logger.error("Servewr error", e);
      // TODO: Don't return stack track to untrusted clients.
      response.sendError(500, toString(e));
      return;
    }

    // Output result.
    response.setStatus(200);
    response.setContentType(encoding.contentType);
    encoding.encode(result, response.getOutputStream());
  }

  private static final String TEXT_PLAIN = "text/plain";

  private void sendError(HttpServletRequest request, HttpServletResponse response, int code,
      String message) throws IOException {
    // Used by App Engine Task Queue.
    if (TEXT_PLAIN.equals(request.getHeader("Accept"))) {
      response.setContentType("text/plain");
      response.getWriter().println(message);
      return;
    }

    response.sendError(code, message);
  }

  private static String toString(Throwable t) {
    StringWriter stackTrace = new StringWriter();
    t.printStackTrace(new PrintWriter(stackTrace, true));
    return stackTrace.toString();
  }

  @Override public void init(FilterConfig filterConfig) {}
  @Override public void destroy() {}
}
