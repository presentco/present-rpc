package present.rpc;

import java.util.Map;

import static present.rpc.Preconditions.checkNotNull;

class RpcService {

  final Class<?> headerType;
  final Object implementation;
  final Map<String, RpcMethod> methods;
  final RpcFilter filter;
  final Map<String, String> aliases;

  <T> RpcService(Class<?> headerType, Class<T> interfaceType, T implementation,
      RpcFilter filter, Map<String, String> aliases) {
    this.headerType = headerType;
    methods = RpcMethod.mapFor(interfaceType);
    this.implementation = checkNotNull(implementation);
    this.filter = filter == null ? RpcInvocation.threadLocalFilter()
        : new RpcFilterChain().add(RpcInvocation.threadLocalFilter()).add(filter);
    this.aliases = aliases;
  }

  Object invoke(Object header, RpcMethod method, Object argument) throws Exception {
    RpcInvocation invocation = RpcInvocation.newInstance(headerType, header, implementation,
        method, argument);
    return filter.filter(invocation);
  }
}
