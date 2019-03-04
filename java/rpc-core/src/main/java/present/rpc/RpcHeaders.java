package present.rpc;

/**
 * Headers from the underlying transport.
 *
 * @author Bob Lee
 */
public interface RpcHeaders {

  /**
   * Sets a header on the underlying transport with the given name and value. Overwrites an
   * existing header with the same name.
   *
   * @throws UnsupportedOperationException if the headers are read-only (as in a server)
   */
  void put(String name, String value);

  /**
   * Returns a header value for the given header name, or null if no header is found.
   * Case-insensitive if the underlying transport is case-insensitive.
   */
  String get(String name);
}
