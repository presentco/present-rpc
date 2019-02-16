package present.rpc;

/**
 * Filters RPC requests.
 *
 * @author Bob Lee (bob@present.co)
 */
public interface RpcFilter {

  /** A filter that calls {@link RpcInvocation#proceed()}. */
  RpcFilter NOOP = new RpcFilter() {
    @Override public Object filter(RpcInvocation invocation) throws Exception {
      return invocation.proceed();
    }
  };

  /**
   * Filters the given invocation. The filter may call {@link RpcInvocation#proceed()} to invoke the
   * downstream service or it may service the invocation itself.
   */
  Object filter(RpcInvocation invocation) throws Exception;
}
