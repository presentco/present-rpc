package present.rpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RPC invocation
 *
 * @author Bob Lee (bob@present.co)
 */
public abstract class RpcInvocation {

  private final Class<?> headerType;
  private final RpcMethod method;
  private Object header;
  private final Object argument;
  private final Object implementation;

  public RpcInvocation(Class<?> headerType, RpcMethod method, Object header, Object argument,
      Object implementation) {
    this.headerType = headerType;
    this.method = method;
    this.header = header;
    this.argument = argument;
    this.implementation = implementation;
    Class<?> interfaceType = method.method().getDeclaringClass();
    interfaceType.cast(implementation);
  }

  public RpcInvocation(RpcInvocation original) {
    this(original.headerType, original.getMethod(), original.header, original.getArgument(),
        original.getImplementation());
  }

  /** Returns the abstract RPC method. */
  public RpcMethod getMethod() {
    return method;
  }

  /** Returns the method argument. */
  public Object getArgument() {
    return this.argument;
  }

  /** Returns the request header. */
  public Object getHeader() {
    return header;
  }

  /** Returns the object that implements this service. */
  public Object getImplementation() { return implementation; }

  /** Returns the concrete implementation method. */
  public Method getImplementationMethod() {
    Method method = this.method.method();
    try {
      return this.implementation.getClass().getMethod(
          method.getName(), method.getParameterTypes());
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  /** Sets the request header. */
  public void setHeader(Object header) {
    this.header = headerType.cast(header);
  }

  public <H> H getHeader(Class<H> headerType) {
    return headerType.cast(header);
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

  /**
   * Gets the RpcInvocation for the current thread.
   * @throws NullPointerException if required is true and we are not in the context of an RPC call.
   */
  public static RpcInvocation current(boolean required) {
    RpcInvocation rpcInvocation = current.get();
    return required ? Preconditions.checkNotNull(rpcInvocation) : rpcInvocation;
  }

  /** Returns a filter that sets the invocation on the current thread. */
  public static RpcFilter threadLocalFilter() {
    return new RpcFilter() {
      @Override public Object filter(RpcInvocation invocation) throws Exception {
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
  public static RpcInvocation newInstance(Class<?> headerType, Object header,
      final Object implementation, final RpcMethod method, final Object argument) {
    headerType.cast(header);
    method.method().getParameterTypes()[0].cast(argument);
    return new RpcInvocation(headerType, method, header, argument, implementation) {
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
        + "headerType="
        + headerType
        + ", method="
        + method
        + ", header="
        + header
        + ", argument="
        + argument
        + '}';
  }
}
