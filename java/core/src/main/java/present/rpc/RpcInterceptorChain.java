package present.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Chains RpcInterceptors.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcInterceptorChain implements RpcInterceptor {

  private final List<RpcInterceptor> interceptors = new ArrayList<>();

  /** Adds a new interceptor to the end of the chain. */
  public RpcInterceptorChain add(RpcInterceptor interceptor) {
    interceptors.add(interceptor);
    return this;
  }

  @Override public Object intercept(RpcInvocation invocation) throws Exception {
    return new ChainedInvocation(invocation).proceed();
  }

  private class ChainedInvocation extends RpcInvocation {

    private final RpcInvocation original;
    private int index = 0;

    private ChainedInvocation(RpcInvocation original) {
      super(original);
      this.original = original;
    }

    @Override public Object proceed() throws Exception {
      if (index < interceptors.size()) {
        try {
          return interceptors.get(index++).intercept(this);
        } finally {
          index--;
        }
      } else {
        return original.proceed();
      }
    }

    @Override public String toString() {
      return original.toString();
    }
  }
}
