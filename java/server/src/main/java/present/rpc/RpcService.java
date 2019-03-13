package present.rpc;

import java.util.Map;

import static present.rpc.Preconditions.checkNotNull;

class RpcService {

  final Class<?> interfaceType;
  final Object implementation;
  final Map<String, RpcMethod> methods;
  final RpcInterceptor interceptor;
  final Map<String, String> aliases;

  <T> RpcService(Class<T> interfaceType, T implementation,
      RpcInterceptor interceptor, Map<String, String> aliases) {
    this.interfaceType = interfaceType;
    methods = RpcMethod.mapFor(interfaceType);
    this.implementation = checkNotNull(implementation);
    this.interceptor = interceptor == null ? RpcInvocation.threadLocalFilter()
        : new RpcInterceptorChain().add(RpcInvocation.threadLocalFilter()).add(interceptor);
    this.aliases = aliases;
  }

  Object invoke(RpcHeaders headers, RpcMethod method, Object argument) throws Exception {
    RpcInvocation invocation = RpcInvocation.newInstance(headers, implementation, method, argument);
    return interceptor.intercept(invocation);
  }

  Object name() {
    return interfaceType.getSimpleName();
  }
}
