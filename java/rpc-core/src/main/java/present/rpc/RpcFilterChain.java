package present.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Chains RpcFilters.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcFilterChain implements RpcFilter {

  private final List<RpcFilter> filters = new ArrayList<>();

  /** Adds a new filter to the end of the chain. */
  public RpcFilterChain add(RpcFilter filter) {
    filters.add(filter);
    return this;
  }

  @Override public Object filter(RpcInvocation invocation) throws Exception {
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
      if (index < filters.size()) {
        try {
          return filters.get(index++).filter(this);
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
