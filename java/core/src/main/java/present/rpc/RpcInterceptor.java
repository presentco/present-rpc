package present.rpc;

/**
 * Intercepts RPC invocations.
 *
 * @author Bob Lee (bob@present.co)
 */
public interface RpcInterceptor {

  /** An interceptor that calls {@link RpcInvocation#proceed()}. */
  RpcInterceptor NOOP = RpcInvocation::proceed;

  /**
   * Intercepts the given invocation. The interceptor may call {@link RpcInvocation#proceed()} to
   * invoke the downstream service or it may service the invocation itself.
   */
  Object intercept(RpcInvocation invocation) throws Exception;
}
