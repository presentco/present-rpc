package present.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RPC invocation
 *
 * @author Bob Lee (bob@present.co)
 */
public abstract class RpcInvocation {

  private final RpcHeaders headers;
  private final RpcMethod method;
  private final Object argument;
  private final Object implementation;

  public RpcInvocation(RpcHeaders headers, RpcMethod method, Object argument,
      Object implementation) {
    this.headers = headers;
    this.method = method;
    this.argument = argument;
    this.implementation = implementation;
    Class<?> interfaceType = method.method().getDeclaringClass();
    interfaceType.cast(implementation);
  }

  public RpcInvocation(RpcInvocation original) {
    this(original.headers, original.method(), original.argument(),
        original.implementation());
  }

  /** Exposes headers from the underyling transport (HTTP, for example). */
  public RpcHeaders headers() {
    return this.headers;
  }

  /** Returns the abstract RPC method. */
  public RpcMethod method() {
    return method;
  }

  /** Returns the method argument. */
  public Object argument() {
    return this.argument;
  }

  /** Returns the object that implements this service. */
  public Object implementation() { return implementation; }

  /** Returns the concrete implementation method. */
  public Method implementationMethod() {
    Method method = this.method.method();
    try {
      return this.implementation.getClass().getMethod(
          method.getName(), method.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract Object proceed() throws Exception;

  private static final ThreadLocal<RpcInvocation> current = new ThreadLocal<>();

  /**
   * Gets the RpcInvocation for the current thread.
   * @throws NullPointerException if we are not in the context of an RPC call.
   */
  public static RpcInvocation current() {
    return Preconditions.checkNotNull(current.get());
  }

  /** Returns an interceptor that sets the invocation on the current thread. */
  static RpcInterceptor threadLocalFilter() {
    return new RpcInterceptor() {
      @Override public Object intercept(RpcInvocation invocation) throws Exception {
        current.set(invocation);
        try {
          return invocation.proceed();
        } finally {
          current.remove();
        }
      }
    };
  }

  /**
   * Creates a new invocation that reflectively invokes the given method on the given
   * implementation instance.
   */
  public static RpcInvocation newInstance(RpcHeaders headers,
      final Object implementation, final RpcMethod method, final Object argument) {
    method.method().getParameterTypes()[0].cast(argument);
    return new RpcInvocation(headers, method, argument, implementation) {
      public Object proceed() throws Exception {
        try {
          return method.method().invoke(implementation, argument);
        } catch (InvocationTargetException e) {
          Throwable cause = e.getCause();
          if (cause instanceof Exception) throw (Exception) cause;
          throw e;
        }
      }
    };
  }

  @Override public String toString() {
    return "RpcInvocation{"
        + ", method="
        + method
        + ", argument="
        + argument
        + '}';
  }
}
