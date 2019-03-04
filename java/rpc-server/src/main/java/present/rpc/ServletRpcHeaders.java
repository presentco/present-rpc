package present.rpc;

import javax.servlet.http.HttpServletRequest;

class ServletRpcHeaders implements RpcHeaders {

  private final HttpServletRequest request;

  ServletRpcHeaders(HttpServletRequest request) {
    this.request = request;
  }

  @Override public void put(String name, String value) {
    throw new UnsupportedOperationException("Servlet headers are read-only.");
  }

  @Override public String get(String name) {
    return request.getHeader(name);
  }
}
