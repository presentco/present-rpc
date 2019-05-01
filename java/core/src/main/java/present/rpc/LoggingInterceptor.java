package present.rpc;

import java.util.logging.Logger;

/**
 * Logs RPC requests.
 *
 * @author Bob Lee
 */
public class LoggingInterceptor implements RpcInterceptor {

  private static final Logger logger = Logger.getLogger("PresentRpc");

  @Override public Object intercept(RpcInvocation invocation) throws Exception {
    RpcMethod method = invocation.method();
    logger.info(method.toString() + "(" + GsonLogging.toJson(invocation.argument()) + ")");
    return invocation.proceed();
  }
}
